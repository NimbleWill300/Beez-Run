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


public class Abeille {

//    protected Carte laCarte;
    private SpriteSheet SpriteSheet;
    
    private int currentFrame = 0;   // 0,1,2 (relativo aos frames 2,3,4)
    int frameIndex = 2;
    private int tick = 0;           // contador de updates
    private final int ticksPerFrame = 5; // ajusta velocidade da animação

//   (Carte laCarte) dans le parentes
    public Abeille() throws IOException {

//        this.laCarte = laCarte;

        this.SpriteSheet = new SpriteSheet();

    }

    public enum BeeState {

        FLY_0(2),       //0 - flying with 0 pollen
        FLY_1(5),       //1 - flying with 1 pollen
        FLY_2(8),       //2 - flying with 2 pollen
        FLY_3(11),      //3 - flying with 3 pollen
        HIT(0),         //4 - hit taken
        DEAD(1);        //5 - dead

        private final int value;

        BeeState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
    
    BeeState etat = BeeState.FLY_0;
    
    public void miseAJour() {
         // animação de voo: usar frames 2,3,4
        tick++;
        if (tick >= ticksPerFrame) {
            tick = 0;
            currentFrame = (currentFrame + 1) % 3; // 0,1,2
//            frameIndex = 2 + currentFrame;     // 2,3,4
        }     
    }

    
    public void rendu(Graphics2D contexte) {

        try {

            Connection connexion = SingletonJDBC.getInstance().getConnection();

            PreparedStatement requete = connexion.prepareStatement("SELECT pseudo, x, y, connecte, direction, couleur, etat FROM abeille");
            ResultSet resultat = requete.executeQuery();
            
            while (resultat.next()) {
                String pseudo = resultat.getString("pseudo");
                double x = resultat.getDouble("x");
                double y = resultat.getDouble("y");
                boolean status = resultat.getBoolean("connecte");
                boolean faceDroite = resultat.getBoolean("direction");
                String couleur = resultat.getString("couleur");
                int etatAbeille = resultat.getInt("etat");

                if(status){
                    BufferedImage sprite;
                    switch (etatAbeille) {
                        case 0:
                            frameIndex = currentFrame + BeeState.FLY_0.getValue();
                            break;
                        case 1:
                            frameIndex = currentFrame + BeeState.FLY_1.getValue();
                            break;
                        case 2:
                            frameIndex = currentFrame + BeeState.FLY_2.getValue();
                            break;
                        case 3:
                            frameIndex = currentFrame + BeeState.FLY_3.getValue();
                            break;
                        case 4:
                            frameIndex = BeeState.HIT.getValue();
                            break;
                        case 5:
                            frameIndex = BeeState.DEAD.getValue();
                            break;
                        default:
                            frameIndex = currentFrame + BeeState.FLY_0.getValue();
                            break;
                    }
                    
                    sprite = SpriteSheet.getFrame(couleur, frameIndex);

                    if(!faceDroite){
                        contexte.drawImage(sprite, (int) x, (int) y, null);
                    }else{
                        contexte.drawImage(sprite, (int)x + sprite.getWidth(null), (int)y, -sprite.getWidth(null), sprite.getHeight(null), null);
                    }
                }
                
            }

            requete.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

}
