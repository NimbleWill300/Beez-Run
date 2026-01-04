package jeu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class HUD {

    private Avatar avatar;

    private BufferedImage heartFull;
    private BufferedImage heartEmpty;
    private BufferedImage pollenIcon;

    private static final int MAX_PV = 5;
    private static final int MAX_POLLEN_ICONS = 3;

    // Taille fixe du HUD
    private static final int HUD_X = 6;
    private static final int HUD_Y = 6;
    private static final int HUD_W = 290;
    private static final int HUD_H = 92;

    public HUD(Avatar avatar) {
        this.avatar = avatar;

        try {
            heartFull  = ImageIO.read(getClass().getResourceAsStream("/resources/heart_full.png"));
            heartEmpty = ImageIO.read(getClass().getResourceAsStream("/resources/heart_empty.png"));
            pollenIcon = ImageIO.read(getClass().getResourceAsStream("/resources/pollen.png"));
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void rendu(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                           RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // ---- Fond fixe translucide ----
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(HUD_X, HUD_Y, HUD_W, HUD_H, 12, 12);

        // ---- Texte (labels) ----
        g.setFont(new Font("Arial", Font.BOLD, 16));

        // Petite ombre pour la lisibilité
        drawLabel(g, "PV:", HUD_X + 10, HUD_Y + 28);
        drawLabel(g, "Pollen:", HUD_X + 10, HUD_Y + 70);

        // ---- Coeurs ----
        int pv = clamp(avatar.getPv(), 0, MAX_PV);

        int heartSize = 30;
        int heartSpacing = 34;

        int xHearts = HUD_X + 70;   // depois do texto "PV:"
        int yHearts = HUD_Y + 12;

        for (int i = 0; i < MAX_PV; i++) {
            BufferedImage img = (i < pv) ? heartFull : heartEmpty;
            if (img != null) {
                g.drawImage(img, xHearts + i * heartSpacing, yHearts, heartSize, heartSize, null);
            }
        }

        // ---- Pollens (0..3 icônes) ----
        int pollenIcons = clamp(avatar.getPollen(), 0, MAX_POLLEN_ICONS);

        int pollenSize = 28;
        int pollenSpacing = 32;

        int xPollen = HUD_X + 90;   // depois do texto "Pollen:"
        int yPollen = HUD_Y + 54;

        if (pollenIcons > 0 && pollenIcon != null) {
            for (int i = 0; i < pollenIcons; i++) {
                g.drawImage(pollenIcon, xPollen + i * pollenSpacing, yPollen, pollenSize, pollenSize, null);
            }
        }
        // se pollenIcons == 0: não desenha nada (só fica o "Pollen:")
    }

    private void drawLabel(Graphics2D g, String text, int x, int y) {
        g.setColor(Color.BLACK);
        g.drawString(text, x + 1, y + 1);
        g.setColor(Color.WHITE);
        g.drawString(text, x, y);
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
