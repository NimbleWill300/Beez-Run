/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jeu;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author nrodrigu
 */
public class SpriteSheet_frelon {
    
    public static final int FRAME_SIZE = 100;

   // private BufferedImage sheet;
    private BufferedImage[] frames;
   // private int currentFrame = 2; 
    
    
      public enum BeeState {
        FLY_0_START(0), // 1,2,3
        HIT(4); // 4,5


        public final int startIndex;

        BeeState(int startIndex) {
            this.startIndex = startIndex;
        }
    }

    public SpriteSheet_frelon() throws IOException {

        //sheet = ImageIO.read(getClass().getResource("../resources/blue_bee.png"));
        BufferedImage sheet = ImageIO.read(getClass().getResource("../resources/frelon.png"));


        int rows = 3;
        int colsPerRow[] = {2, 2, 1}; // colunas por linha

        frames = new BufferedImage[5];  // seus 5 estados

        int index = 0;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < colsPerRow[row]; col++) {

                if (index >= 5) break; // já pegou todos os sprites necessários

                frames[index] = sheet.getSubimage(
                        col * FRAME_SIZE,
                        row * FRAME_SIZE,
                        FRAME_SIZE,
                        FRAME_SIZE
                );

                index++;
            }
        }
    }

    
//    public void updateSprite(){
//    
//    
//    
//    }

    
    public BufferedImage getFrame(int index) {
        return frames[index];
    }
    
   
    
}

