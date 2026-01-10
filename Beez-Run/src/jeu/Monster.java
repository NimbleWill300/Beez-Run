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
    private int spriteSize = 100;

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
    
//   (Carte laCarte) dans le parentes

    public Monster() throws IOException {

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
                        double dx = xA - x_frelon;
                        double dy = yA - y_frelon;
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
            if(damageDelay == 0){ // Etat normal, il peut bouger
                if (dist <= detectionRadius) {

                    // perseguir abelha mais próxima
                    double dx = targetX - x_frelon;
                    double dy = targetY - y_frelon;

                    double len = Math.sqrt(dx * dx + dy * dy);

                    if (len > 0.1) {
                        x_frelon += (dx / len) * vitesse;
                        y_frelon += (dy / len) * vitesse;
                    }

                } else {
                    // movimento padrão em quadrado
                    if (y_frelon <= squareSizeMin && x_frelon < squareSizeMax) {
                        x_frelon += vitesse;
                    } else if (x_frelon >= squareSizeMax && y_frelon < squareSizeMax) {
                        y_frelon += vitesse;
                    } else if (y_frelon >= squareSizeMax && x_frelon > squareSizeMin) {
                        x_frelon -= vitesse;
                    } else if (x_frelon <= squareSizeMin && y_frelon > squareSizeMin) {
                        y_frelon -= vitesse;
                    }else{
                        x_frelon -= vitesse;
                    }
                }
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
                    damageDelay -= 1;
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

    public void lancer() {
        this.x_frelon = 50;
        this.y_frelon = 50;
    }

    public double getX() { return x_frelon; }
    public double getY() { return y_frelon; }

    public double getLargeur() { return sprite.getHeight(); }
    public double getHauteur() { return sprite.getWidth(); }
}