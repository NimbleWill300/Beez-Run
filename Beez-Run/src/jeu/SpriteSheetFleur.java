package jeu;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class SpriteSheetFleur {

    public static final int FRAME_SIZE = 57;

    private final BufferedImage[] frames = new BufferedImage[2]; // 0=colorida, 1=cinza

    public SpriteSheetFleur() throws IOException {
        BufferedImage sheet = ImageIO.read(getClass().getResource("/resources/sprite_flower1.png"));
        // sua imagem: 2 linhas x 1 coluna (64x64 cada)

        frames[0] = sheet.getSubimage(0, 0, FRAME_SIZE, FRAME_SIZE);                // colorida
        frames[1] = sheet.getSubimage(0, FRAME_SIZE, FRAME_SIZE, FRAME_SIZE);      // cinza
    }

    public BufferedImage getFrame(int index) {
        return frames[index];
    }
}
