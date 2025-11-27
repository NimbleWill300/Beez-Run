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

    protected int squareSizeMax = 400;
    protected int squareSizeMin = 50;
    protected int vitesse = 2;

    protected double detectionRadius = 220;  // raio de detecção
    protected double dist = Double.MAX_VALUE;

    protected double minDist = Double.MAX_VALUE;
    protected double targetX = 0;
    protected double targetY = 0;

    public Monster() {
        try {
            this.sprite = ImageIO.read(getClass().getResource("../resources/ferlon.png"));
        } catch (IOException ex) {
            Logger.getLogger(Monster.class.getName()).log(Level.SEVERE, null, ex);
        }
        lancer();
    }

    public void miseAJour() {

        try {
            Connection connexion = SingletonJDBC.getInstance().getConnection();

            // 1) Carregar posição atual do frelon no banco
            PreparedStatement requeteF = connexion.prepareStatement("SELECT x, y FROM frelon WHERE nom = ?");
            requeteF.setString(1, "frelon2");
            ResultSet resF = requeteF.executeQuery();

            if (resF.next()) {
                x_frelon = resF.getDouble("x");
                y_frelon = resF.getDouble("y");
            }

            requeteF.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // resetar valores de busca
        minDist = Double.MAX_VALUE;
        dist = Double.MAX_VALUE;

        String names = "abeille1 abeille2 abeille3 abeille4";

        try {
            Connection connexion = SingletonJDBC.getInstance().getConnection();

            // 2) Procurar abelha mais próxima
            PreparedStatement requete = connexion.prepareStatement("SELECT pseudo, x, y, connecte FROM abeille");
            ResultSet resultat = requete.executeQuery();

            while (resultat.next()) {

                String pseudo = resultat.getString("pseudo");
                double xA = resultat.getDouble("x");
                double yA = resultat.getDouble("y");
                boolean connecte = resultat.getBoolean("connecte");

                // só considera abelhas da lista e conectadas
                if (names.contains(pseudo) && connecte) {

                    // distância Manhattan
                    double dx = xA - x_frelon;
                    double dy = yA - y_frelon;
                    double distTmp = Math.abs(dx) + Math.abs(dy);

                    // dentro do raio de detecção?
                    if (distTmp <= detectionRadius && distTmp < minDist) {
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
            }
        }
    }

    public void rendu(Graphics2D contexte) {

        try {
            Connection connexion = SingletonJDBC.getInstance().getConnection();

            PreparedStatement requete = connexion.prepareStatement("UPDATE frelon SET x = ?, y = ? WHERE nom = ?");
            requete.setDouble(1, x_frelon);
            requete.setDouble(2, y_frelon);
            requete.setString(3, "frelon2");

            requete.executeUpdate();
            requete.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        contexte.drawImage(this.sprite, (int) x_frelon, (int) y_frelon, null);
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