package jeu;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import outils.SingletonJDBC;


public class Monster {

    protected BufferedImage sprite;
    protected double x_frelon, y_frelon;
    private int direction = 0;
    private static int spriteSize = 100;
    
    // -------- Hitbox (ajuste si besoin) --------
    // Sprite tile = 64x64, on prend une hitbox plus petite pour éviter "accrocher" partout
    private static final int HIT_W = spriteSize/2;
    private static final int HIT_H = spriteSize/2;
    private static final int HIT_OX = HIT_W/2;  // offset à partir de x
    private static final int HIT_OY = HIT_H/2;  // offset à partir de y

    protected int squareSizeMax = 400;
    protected int squareSizeMin = 50;
    protected int vitesse = 5;

    protected double detectionRadius = 220;  // raio de detecção
    protected double damageRadius = 20;  // raio de detecção
    protected double dist = Double.MAX_VALUE;

    protected double minDist = Double.MAX_VALUE;
    protected double targetX = 0;
    protected double targetY = 0;
    
    private SpriteSheet_frelon uneSpriteSheet_frelon;
    
    private int currentFrame = 0;   // 0,1,2 (relativo aos frames 2,3,4)
    private int tick = 0;           // contador de updates
    private final int ticksPerFrame = 3; // ajusta velocidade da animação
    
    int etat = 0;
    private int damageDelay = 0;
    private int delay = 30;
    
    // -------- Référence carte --------
    private final Carte carte;
    
    // -------- Mouvement aléatoire --------
    private double randDirX = 0;
    private double randDirY = 0;
    private int randTimer = 0;
    private final int RAND_TIME_MAX = 60; // ticks antes de trocar direção

    // -------- Limites du monde (pixels) --------
    private final int WORLD_MIN_X = 0;
    private final int WORLD_MIN_Y = 0;
    private final int WORLD_MAX_X = 1280; // largeur de la carte
    private final int WORLD_MAX_Y = 960; // hauteur de la carte


    public Monster(Carte carte) throws IOException {

        this.carte = carte;
        this.uneSpriteSheet_frelon = new SpriteSheet_frelon();
        this.sprite = uneSpriteSheet_frelon.getFrame(0);
    }

    public void miseAJour() {
        double x = x_frelon;
        // update frelon
        try {
            Connection connexion = SingletonJDBC.getInstance().getConnection();

            // 1) Carregar posição atual do frelon no banco
            PreparedStatement requeteF = connexion.prepareStatement("SELECT x, y, etat FROM frelon WHERE nom = ?");
            requeteF.setString(1, "frelon2");
            ResultSet resF = requeteF.executeQuery();

            if (resF.next()) {
                x_frelon = resF.getDouble("x");
                y_frelon = resF.getDouble("y");
                etat = resF.getInt("etat");
            }

            requeteF.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        if(etat == 1){
            if(damageDelay > 0){
                damageDelay -= 1;
                etat = 1;
            }else{
                etat = 0;
            }
        }else{
            // resetar valores de busca
            minDist = Double.MAX_VALUE;
            dist = Double.MAX_VALUE;

            try {
                Connection connexion = SingletonJDBC.getInstance().getConnection();

                // 2) Procurar abelha mais próxima
                PreparedStatement requete = connexion.prepareStatement("SELECT pseudo, x, y, connecte, etat FROM abeille");
                ResultSet resultat = requete.executeQuery();

                while (resultat.next()) {

                    String pseudo = resultat.getString("pseudo");
                    double xA = resultat.getDouble("x");
                    double yA = resultat.getDouble("y");
                    boolean connecte = resultat.getBoolean("connecte");
                    int etat_abeille = resultat.getInt("etat");

                    // só considera abelhas da lista e conectadas
                    if (connecte && etat_abeille != 5) {

                        // distância Manhattan
                        double dx = xA - x_frelon + HIT_OX;
                        double dy = yA - y_frelon + HIT_OY;
                        double distTmp = Math.abs(dx) + Math.abs(dy);

                        // dentro do raio de detecção?
                        if (distTmp <= detectionRadius && distTmp < minDist) {
                            if(distTmp <= damageRadius){ // hits abeille
                                if(etat == 0){
                                    damageDelay = delay;
                                    etat = 1;
                                }

                                PreparedStatement requete1 = connexion.prepareStatement("UPDATE abeille SET etat = ? WHERE pseudo = ?");
                                requete1.setInt(1, 4); // changes bee state to 'hit'
                                requete1.setString(2, pseudo);

                                requete1.executeUpdate();
                                requete1.close();
                            }
                            minDist = distTmp;
                            dist = distTmp;
                            targetX = xA;
                            targetY = yA;
                        }
                    }
                }
                requete.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            // 3) MOVER — perseguir ou patrulhar
            if (damageDelay == 0) {

                double nx = x_frelon;
                double ny = y_frelon;

                if (dist <= detectionRadius) {
                    // ====== POURSUIVRE ABEILLE ======
                    double dx = targetX - x_frelon;
                    double dy = targetY - y_frelon;

                    double len = Math.sqrt(dx * dx + dy * dy);
                    if (len > 0.1) {
                        nx += (dx / len) * vitesse;
                        ny += (dy / len) * vitesse;
                    }

                } else {
                    // ====== MOUVEMENT ALÉATOIRE ======
                    if (randTimer <= 0 || (randDirX == 0 && randDirY == 0)) {
                        chooseRandomDirection();
                    }

                    nx += randDirX * vitesse;
                    ny += randDirY * vitesse;
                    randTimer--;
                }

                // ====== COLLISIONS (SLIDE) ======
                if (!collidesAt(nx, y_frelon)) {
                    x_frelon = nx;
                } else {
                    x_frelon = pushOutX(x_frelon, nx, y_frelon);
                    chooseRandomDirection(); // rebond
                }

                if (!collidesAt(x_frelon, ny)) {
                    y_frelon = ny;
                } else {
                    y_frelon = pushOutY(y_frelon, ny, x_frelon);
                    chooseRandomDirection(); // rebond
                }
                // ====== Limites de la carte ======
                x_frelon = clamp(x_frelon, WORLD_MIN_X - HIT_OX, WORLD_MAX_X - HIT_OX - HIT_W);
                y_frelon = clamp(y_frelon, WORLD_MIN_Y - HIT_OY, WORLD_MAX_Y - HIT_OY - HIT_H);
            }

            if(x_frelon < x){
                direction = 0;
            }else if(x_frelon > x){
                direction = 1;
            }
        }
        
        try {
            Connection connexion = SingletonJDBC.getInstance().getConnection();

            PreparedStatement requete = connexion.prepareStatement("UPDATE frelon SET x = ?, y = ?, etat = ?, direction = ? WHERE nom = ?");
            requete.setDouble(1, x_frelon);
            requete.setDouble(2, y_frelon);
            requete.setDouble(3, etat);
            requete.setDouble(4, direction);
            requete.setString(5, "frelon2");

            requete.executeUpdate();
            requete.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        updateFrame();
    }

    public void rendu(Graphics2D contexte) {
        if(direction == 1){
            contexte.drawImage(this.sprite, (int) x_frelon + spriteSize, (int) y_frelon, -spriteSize, spriteSize, null);
        }else{
            contexte.drawImage(this.sprite, (int) x_frelon, (int) y_frelon, null);
        }
    }
    
    private void updateFrame(){
        // animação de voo: usar frames 2,3,4
        tick++;
        if (tick >= ticksPerFrame) {
            tick = 0;
            if(etat == 0){
                currentFrame = (currentFrame + 1) % 3; // 0,1,2
                this.sprite = uneSpriteSheet_frelon.getFrame(currentFrame);
            }else{
                if(damageDelay > 0){
                    etat = 1;
                    currentFrame = (currentFrame + 1) % 2; // 3, 4
                    this.sprite = uneSpriteSheet_frelon.getFrame(currentFrame + 3);
                }else{
                    etat = 0;
                    try {
                        Connection connexion = SingletonJDBC.getInstance().getConnection();

                        PreparedStatement requete = connexion.prepareStatement("UPDATE frelon SET etat = ? WHERE nom = ?");
                        requete.setInt(1, etat);
                        requete.setString(2, "frelon2");

                        requete.executeUpdate();
                        requete.close();

                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
    
    private boolean collidesAt(double newX, double newY) {
        double left   = newX + HIT_OX;
        double right  = newX + HIT_OX + HIT_W - 1;
        double top    = newY + HIT_OY;
        double bottom = newY + HIT_OY + HIT_H - 1;

<<<<<<< Updated upstream
    public void spawnAt(double x, double y) {
    this.x_frelon = x;
    this.y_frelon = y;
    this.etat = 0;

    try {
        Connection c = SingletonJDBC.getInstance().getConnection();
        PreparedStatement st = c.prepareStatement(
            "UPDATE frelon SET x = ?, y = ?, etat = 0 WHERE nom = ?"
        );
        st.setDouble(1, x);
        st.setDouble(2, y);
        st.setString(3, "frelon2");
        st.executeUpdate();
        st.close();
    } catch (SQLException e) {
        e.printStackTrace();
=======
        return carte.isSolidPixel(left, top)
            || carte.isSolidPixel(right, top)
            || carte.isSolidPixel(left, bottom)
            || carte.isSolidPixel(right, bottom);
    }

    // Si tu bloques en X, on tente de sortir un peu (optionnel)
    private double pushOutX(double oldX, double targetX, double yFixed) {
        // on avance/recul 1px à la fois vers targetX jusqu'à collision, puis on revient
        double step = (targetX > oldX) ? 1 : -1;
        double xTest = oldX;
        while (xTest != targetX) {
            double next = xTest + step;
            if (collidesAt(next, yFixed)) {
                return xTest;
            }
            xTest = next;
            if (Math.abs(xTest - targetX) < 0.5) break;
        }
        return xTest;
    }

    private double pushOutY(double oldY, double targetY, double xFixed) {
        double step = (targetY > oldY) ? 1 : -1;
        double yTest = oldY;
        while (yTest != targetY) {
            double next = yTest + step;
            if (collidesAt(xFixed, next)) {
                return yTest;
            }
            yTest = next;
            if (Math.abs(yTest - targetY) < 0.5) break;
        }
        return yTest;
    }

    // Empêche de spawn dans un mur (simple)
    private void resolveIfSpawnInWall() {
        if (!collidesAt(x_frelon, y_frelon)) return;

        // essaie de trouver un spot proche (petite recherche)
        for (int r = 0; r < 10; r++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dx = -r; dx <= r; dx++) {
                    double nx = x_frelon + dx * 5;
                    double ny = y_frelon + dy * 5;
                    if (!collidesAt(nx, ny)) {
                        x_frelon = nx;
                        y_frelon = ny;
                        return;
                    }
                }
            }
        }
        // sinon, laisse tel quel (debug)
    }

    private void chooseRandomDirection() {
        double angle = Math.random() * 2 * Math.PI;
        randDirX = Math.cos(angle);
        randDirY = Math.sin(angle);
        randTimer = RAND_TIME_MAX;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
>>>>>>> Stashed changes
    }
}

    public double getX() { return x_frelon; }
    public double getY() { return y_frelon; }

    public double getLargeur() { return sprite.getHeight(); }
    public double getHauteur() { return sprite.getWidth(); }
}

