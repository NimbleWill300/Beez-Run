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
public class SpriteSheet_carte {
    
    public static final int FRAME_SIZE = 64;

   // private BufferedImage sheet;
    private BufferedImage[] frames;
    

    public SpriteSheet_carte() throws IOException {

        BufferedImage sheetCarte;
        
        sheetCarte = ImageIO.read(getClass().getResource("../resources/tileset_map1.png"));

        int rows = 36;
        int colsPerRow = 40; 
        
        frames = new BufferedImage[rows * colsPerRow];

        int index = 0;

        for(int i = 0; i < rows; i++){
            for (int j = 0; j < colsPerRow; j++) {
                this.frames[index] = sheetCarte.getSubimage(
                                    j * FRAME_SIZE,
                                    i * FRAME_SIZE,
                                    FRAME_SIZE,
                                    FRAME_SIZE);
                index++;
            }
        }
    }

    
    
    public BufferedImage getFrame(int index) {
        return frames[index];
    }
}