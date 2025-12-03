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
public class SpriteSheet {
    
    public static final int FRAME_SIZE = 64;

   // private BufferedImage sheet;
    private BufferedImage[][] frames;
   // private int currentFrame = 2; 
    
    
      public enum BeeState {
        HIT(0),
        DEAD(1),
        FLY_0_START(2), // 2,3,4
        FLY_1_START(5), // 5,6,7
        FLY_2_START(8), // 8,9,10
        FLY_3_START(11); // 11,12,13

        public final int startIndex;

        BeeState(int startIndex) {
            this.startIndex = startIndex;
        }
    }

    public SpriteSheet() throws IOException {

        BufferedImage sheet_vert, sheet_mauve, sheet_rouge, sheet_bleu;
        
        sheet_vert = ImageIO.read(getClass().getResource("../resources/green_bee.png"));
        sheet_mauve = ImageIO.read(getClass().getResource("../resources/purple_bee.png"));
        sheet_rouge = ImageIO.read(getClass().getResource("../resources/red_bee.png"));
        sheet_bleu = ImageIO.read(getClass().getResource("../resources/blue_bee.png"));
        
//        switch (couleur) {
//            case "vert":
//                sheet = ImageIO.read(getClass().getResource("../resources/green_bee.png"));
//                break;
//            case "mauve":
//                sheet = ImageIO.read(getClass().getResource("../resources/purple_bee.png"));
//                break;
//            case "rouge":
//                sheet = ImageIO.read(getClass().getResource("../resources/red_bee.png"));
//                break;
//            default:
//                sheet = ImageIO.read(getClass().getResource("../resources/blue_bee.png"));
//                break;
//        }

        int rows = 4;
        int colsPerRow[] = {4, 4, 4, 3}; // colunas por linha

        frames = new BufferedImage[4][14];  // seus 14 estados

        int index = 0;

        for(int i = 0; i < 4; i++){
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < colsPerRow[row]; col++) {

                    if (index >= 14) break; // já pegou todos os sprites necessários

                    switch(i){
                        case 0:
                            frames[i][index] = sheet_vert.getSubimage(
                                    col * FRAME_SIZE,
                                    row * FRAME_SIZE,
                                    FRAME_SIZE,
                                    FRAME_SIZE
                            );
                            break;
                        case 1:
                            frames[i][index] = sheet_mauve.getSubimage(
                                    col * FRAME_SIZE,
                                    row * FRAME_SIZE,
                                    FRAME_SIZE,
                                    FRAME_SIZE
                            );
                            break;
                        case 2:
                            frames[i][index] = sheet_rouge.getSubimage(
                                    col * FRAME_SIZE,
                                    row * FRAME_SIZE,
                                    FRAME_SIZE,
                                    FRAME_SIZE
                            );
                            break;
                        case 3:
                            frames[i][index] = sheet_bleu.getSubimage(
                                    col * FRAME_SIZE,
                                    row * FRAME_SIZE,
                                    FRAME_SIZE,
                                    FRAME_SIZE
                            );
                            break;
                    }

                    index++;
                }
            }
            index = 0;
        }
    }

    
    public BufferedImage getFrame(String couleur, int index) {
        switch (couleur) {
            case "vert":
                return frames[0][index];
            case "mauve":
                return frames[1][index];
            case "rouge":
                return frames[2][index];
            default:
                return frames[3][index];
        }
    }
}