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
    private SpriteSheet uneSpriteSheet;
    
    private int currentFrame = 0;   // 0,1,2 (relativo aos frames 2,3,4)
    private int tick = 0;           // contador de updates
    private final int ticksPerFrame = 5; // ajusta velocidade da animação
    

//   (Carte laCarte) dans le parentes
    public Abeille() throws IOException {

//        this.laCarte = laCarte;
        this.uneSpriteSheet = new SpriteSheet();
        this.sprite = uneSpriteSheet.getFrame(2);
    

    }

    public void miseAJour() {
         // animação de voo: usar frames 2,3,4
        tick++;
        if (tick >= ticksPerFrame) {
            tick = 0;
            currentFrame = (currentFrame + 1) % 3; // 0,1,2
            int frameIndex = 2 + currentFrame;     // 2,3,4
            this.sprite = uneSpriteSheet.getFrame(frameIndex);
        }     
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
