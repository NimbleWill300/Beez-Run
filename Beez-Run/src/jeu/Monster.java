package jeu;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import outils.SingletonJDBC;

public class Monster {

    protected BufferedImage sprite;
    protected double x_frelon, y_frelon;

    private int direction = 0;
    private static final int spriteSize = 100;

    // -------- Hitbox --------
    private static final int HIT_W = spriteSize / 2;
    private static final int HIT_H = spriteSize / 2;
    private static final int HIT_OX = HIT_W / 2;
    private static final int HIT_OY = HIT_H / 2;

    protected int vitesse = 5;

    protected double detectionRadius = 220;
    protected double damageRadius = 20;
    protected double dist = Double.MAX_VALUE;

    protected double minDist = Double.MAX_VALUE;
    protected double targetX = 0;
    protected double targetY = 0;

    private final SpriteSheet_frelon uneSpriteSheet_frelon;

    private int currentFrame = 0;
    private int tick = 0;
    private final int ticksPerFrame = 3;

    int etat = 0;
    private int damageDelay = 0;
    private final int delay = 30;

    // -------- Référence carte (collisions) --------
    private final Carte carte;

    // -------- Mouvement aléatoire --------
    private double randDirX = 0;
    private double randDirY = 0;
    private int randTimer = 0;
    private final int RAND_TIME_MAX = 60;

    // -------- Limites du monde --------
    private final int WORLD_MIN_X = 0;
    private final int WORLD_MIN_Y = 0;
    private final int WORLD_MAX_X = 1280;
    private final int WORLD_MAX_Y = 960;

    public Monster(Carte carte) throws IOException {
        this.carte = carte;
        this.uneSpriteSheet_frelon = new SpriteSheet_frelon();
        this.sprite = uneSpriteSheet_frelon.getFrame(0);
    }

    public void miseAJour() {
        double lastX = x_frelon;

        // 1) leitura do DB (fonte da verdade)
        try {
            Connection connexion = SingletonJDBC.getInstance().getConnection();
            PreparedStatement requeteF = connexion.prepareStatement(
                "SELECT x, y, etat FROM frelon WHERE nom = ?"
            );
            requeteF.setString(1, "frelon2");
            ResultSet resF = requeteF.executeQuery();

            if (resF.next()) {
                x_frelon = resF.getDouble("x");
                y_frelon = resF.getDouble("y");
                etat = resF.getInt("etat");
            }

            resF.close();
            requeteF.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // 2) lógica hit delay
        if (etat == 1) {
            if (damageDelay > 0) {
                damageDelay -= 1;
                etat = 1;
            } else {
                etat = 0;
            }
        } else {
            // reset busca
            minDist = Double.MAX_VALUE;
            dist = Double.MAX_VALUE;

            // 3) acha a abelha mais próxima
            try {
                Connection connexion = SingletonJDBC.getInstance().getConnection();
                PreparedStatement requete = connexion.prepareStatement(
                    "SELECT pseudo, x, y, connecte, etat FROM abeille"
                );
                ResultSet resultat = requete.executeQuery();

                while (resultat.next()) {
                    String pseudo = resultat.getString("pseudo");
                    double xA = resultat.getDouble("x");
                    double yA = resultat.getDouble("y");
                    boolean connecte = resultat.getBoolean("connecte");
                    int etat_abeille = resultat.getInt("etat");

                    if (connecte && etat_abeille != 5) {
                        double dx = xA - (x_frelon + HIT_OX);
                        double dy = yA - (y_frelon + HIT_OY);
                        double distTmp = Math.abs(dx) + Math.abs(dy);

                        if (distTmp <= detectionRadius && distTmp < minDist) {
                            if (distTmp <= damageRadius) {
                                if (etat == 0) {
                                    damageDelay = delay;
                                    etat = 1;
                                }

                                PreparedStatement requete1 = connexion.prepareStatement(
                                    "UPDATE abeille SET etat = ? WHERE pseudo = ?"
                                );
                                requete1.setInt(1, 4);
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

                resultat.close();
                requete.close();

            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            // 4) move (pursuit ou random) com collision slide
            if (damageDelay == 0) {
                double nx = x_frelon;
                double ny = y_frelon;

                if (dist <= detectionRadius) {
                    double dx = targetX - x_frelon;
                    double dy = targetY - y_frelon;

                    double len = Math.sqrt(dx * dx + dy * dy);
                    if (len > 0.1) {
                        nx += (dx / len) * vitesse;
                        ny += (dy / len) * vitesse;
                    }

                } else {
                    if (randTimer <= 0 || (randDirX == 0 && randDirY == 0)) {
                        chooseRandomDirection();
                    }

                    nx += randDirX * vitesse;
                    ny += randDirY * vitesse;
                    randTimer--;
                }

                // X slide
                if (!collidesAt(nx, y_frelon)) {
                    x_frelon = nx;
                } else {
                    x_frelon = pushOutX(x_frelon, nx, y_frelon);
                    chooseRandomDirection();
                }

                // Y slide
                if (!collidesAt(x_frelon, ny)) {
                    y_frelon = ny;
                } else {
                    y_frelon = pushOutY(y_frelon, ny, x_frelon);
                    chooseRandomDirection();
                }

                // clamp
                x_frelon = clamp(x_frelon, WORLD_MIN_X - HIT_OX, WORLD_MAX_X - HIT_OX - HIT_W);
                y_frelon = clamp(y_frelon, WORLD_MIN_Y - HIT_OY, WORLD_MAX_Y - HIT_OY - HIT_H);
            }

            // direção do sprite
            if (x_frelon < lastX) direction = 0;
            else if (x_frelon > lastX) direction = 1;
        }

        // 5) grava no DB
        try {
            Connection connexion = SingletonJDBC.getInstance().getConnection();
            PreparedStatement requete = connexion.prepareStatement(
                "UPDATE frelon SET x = ?, y = ?, etat = ?, direction = ? WHERE nom = ?"
            );
            requete.setDouble(1, x_frelon);
            requete.setDouble(2, y_frelon);
            requete.setInt(3, etat);
            requete.setInt(4, direction);
            requete.setString(5, "frelon2");
            requete.executeUpdate();
            requete.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        updateFrame();
    }

    public void rendu(Graphics2D contexte) {
        if (direction == 1) {
            contexte.drawImage(this.sprite, (int) x_frelon + spriteSize, (int) y_frelon, -spriteSize, spriteSize, null);
        } else {
            contexte.drawImage(this.sprite, (int) x_frelon, (int) y_frelon, spriteSize, spriteSize, null);
        }
    }

    // ✅ método de spawn separado e correto (não quebra a classe)
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
        }
    }

    private void updateFrame() {
        tick++;
        if (tick >= ticksPerFrame) {
            tick = 0;
            if (etat == 0) {
                currentFrame = (currentFrame + 1) % 3;
                this.sprite = uneSpriteSheet_frelon.getFrame(currentFrame);
            } else {
                if (damageDelay > 0) {
                    etat = 1;
                    currentFrame = (currentFrame + 1) % 2;
                    this.sprite = uneSpriteSheet_frelon.getFrame(currentFrame + 3);
                } else {
                    etat = 0;
                }
            }
        }
    }

    private boolean collidesAt(double newX, double newY) {
        double left   = newX + HIT_OX;
        double right  = newX + HIT_OX + HIT_W - 1;
        double top    = newY + HIT_OY;
        double bottom = newY + HIT_OY + HIT_H - 1;

        return carte.isSolidPixel(left, top)
            || carte.isSolidPixel(right, top)
            || carte.isSolidPixel(left, bottom)
            || carte.isSolidPixel(right, bottom);
    }

    private double pushOutX(double oldX, double targetX, double yFixed) {
        double step = (targetX > oldX) ? 1 : -1;
        double xTest = oldX;
        while (xTest != targetX) {
            double next = xTest + step;
            if (collidesAt(next, yFixed)) return xTest;
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
            if (collidesAt(xFixed, next)) return yTest;
            yTest = next;
            if (Math.abs(yTest - targetY) < 0.5) break;
        }
        return yTest;
    }

    private void chooseRandomDirection() {
        double angle = Math.random() * 2 * Math.PI;
        randDirX = Math.cos(angle);
        randDirY = Math.sin(angle);
        randTimer = RAND_TIME_MAX;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public double getX() { return x_frelon; }
    public double getY() { return y_frelon; }
}
