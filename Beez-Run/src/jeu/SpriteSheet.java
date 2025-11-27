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

    private BufferedImage sheet;
    private BufferedImage[] frames;

    public SpriteSheet() throws IOException {

        sheet = ImageIO.read(getClass().getResource("/sprites/blue_bee.png"));

        int rows = 4;
        int colsPerRow[] = {4, 4, 4, 3}; // colunas por linha

        frames = new BufferedImage[14];  // seus 14 estados

        int index = 0;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < colsPerRow[row]; col++) {

                if (index >= 14) break; // já pegou todos os sprites necessários

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

    public BufferedImage getFrame(int index) {
        return frames[index];
    }
}

