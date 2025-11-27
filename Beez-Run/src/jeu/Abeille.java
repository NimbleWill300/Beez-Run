package jeu;

import java.awt.Color;
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

/**
 * Exemple de classe lutin
 *
 * @author guillaume.laurent
 */
public class Abeille {

    protected BufferedImage sprite;
//    protected Carte laCarte;

//   (Carte laCarte) dans le parentese
    public Abeille() {
//        this.laCarte = laCarte;
        try {
            this.sprite = ImageIO.read(getClass().getResource("../resources/abeille.png"));
        } catch (IOException ex) {
            Logger.getLogger(Avatar_old.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void miseAJour() {
    }

    String names = "abeille1 abeille2 abeille3 abeille4";
    
    public void rendu(Graphics2D contexte) {

        try {

            Connection connexion = SingletonJDBC.getInstance().getConnection();

            PreparedStatement requete = connexion.prepareStatement("SELECT pseudo, x, y, connecte FROM abeille");
            ResultSet resultat = requete.executeQuery();
            while (resultat.next()) {
                
                String pseudo = resultat.getString("pseudo");
                double x = resultat.getDouble("x");
                double y = resultat.getDouble("y");
                boolean status = resultat.getBoolean("connecte");
                if(names.contains(pseudo)){
                    if(status)
                    contexte.drawImage(this.sprite, (int) x, (int) y, null);
                }
                
            }

            requete.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
//        try {
//            
//            PreparedStatement requete = connexion.prepareStatement("SELECT pseudo, latitude, longitude FROM dresseurs;");
//            ResultSet resultat = requete.executeQuery();
//            while (resultat.next()) {
//                
//                String pseudo = resultat.getString("pseudo");
//                double latitude = resultat.getDouble("latitude");
//                double longitude = resultat.getDouble("longitude");
//                //System.out.println(pseudo + " = (" + latitude + "; " + longitude + ")");
//                
//                int x = laCarte.longitudeEnPixel(longitude);
//                int y = laCarte.latitudeEnPixel(latitude);
//                contexte.setColor(Color.red);
//                contexte.fillOval(x - 5, y - 5, 10, 10);
//                contexte.drawString(pseudo, x + 5, y - 5);
//            }
//
//            requete.close();           
//
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }

    }

}
