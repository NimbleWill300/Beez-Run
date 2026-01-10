package jeu;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Fenêtre principale du jeu
 * Rendu en framebuffer (1280x960) + affichage mis à l'échelle (auto)
 *
 * Mode:
 *  - fenêtre (auto size) OU fullscreen
 *
 * IMPORTANT:
 *  - La logique reste en 1280x960 (positions/collisions)
 *  - Seul l'affichage est mis à l'échelle
 */
public class FenetreDeJeu extends JFrame implements ActionListener, KeyListener {

    // Résolution logique du jeu (ne change pas)
    public static final int LOGICAL_W = 1280;
    public static final int LOGICAL_H = 960;

    // Choix du mode
    private static final boolean FULLSCREEN = false; // ✅ mettre true pour fullscreen

    private BufferedImage framebuffer;
    private Graphics2D fbG;
    private Sound bgm;


    private JPanel panel;
    private Jeu jeu;
    private Timer timer;

    // mise à l'échelle + offset pour letterbox
    private double scale = 1.0;
    private int offsetX = 0;
    private int offsetY = 0;

    public FenetreDeJeu(String name) throws IOException {

        // -------- Framebuffer logique --------
        this.framebuffer = new BufferedImage(LOGICAL_W, LOGICAL_H, BufferedImage.TYPE_INT_ARGB);
        this.fbG = framebuffer.createGraphics();

        // -------- Panel de rendu (dessine le framebuffer mis à l'échelle) --------
        this.panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Dessine le framebuffer mis à l'échelle et centré
                Graphics2D g2 = (Graphics2D) g;
                g2.drawImage(
                        framebuffer,
                        offsetX, offsetY,
                        (int) Math.round(LOGICAL_W * scale),
                        (int) Math.round(LOGICAL_H * scale),
                        null
                );
            }
        };

        this.setTitle("Beez Run");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setContentPane(panel);

        // -------- Taille fenêtre / fullscreen --------
        if (FULLSCREEN) {
            // Fullscreen exclusif
            this.setUndecorated(true);
            this.setExtendedState(JFrame.MAXIMIZED_BOTH);
            this.setVisible(true); // Important pour avoir la bonne taille de panel
        } else {
            // Fenêtre auto-ajustée à la taille de l'écran (avec marge)
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            int screenW = screen.width;
            int screenH = screen.height;

            // On laisse une petite marge (barre Windows, etc.)
            int targetW = (int) (screenW * 0.95);
            int targetH = (int) (screenH * 0.95);

            panel.setPreferredSize(new Dimension(targetW, targetH));
            this.pack();
            this.setLocationRelativeTo(null);
            this.setVisible(true);
        }

        // Calcul initial scale/offset
        computeScaleAndOffsets();

        // -------- Jeu --------
        this.jeu = new Jeu(name);
        
        this.bgm = new Sound();
        this.bgm.playLoopFromResource("/resources/sound_background_loop.wav");
        this.bgm.setVolume(0.35f); // ajuste como quiser (0.0 a 1.0)


        // -------- Timer (boucle de jeu ~25 FPS) --------
        this.timer = new Timer(40, this);
        this.timer.start();

        // -------- Input clavier --------
        this.addKeyListener(this);
        this.setFocusable(true);
        this.requestFocusInWindow();

        // -------- Recalculer scale si on change de taille (utile en fullscreen / multi-écran) --------
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                computeScaleAndOffsets();
            }
        });

        // -------- Fermeture propre --------
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Fermeture de la fenêtre...");

                FenetreDeJeu.this.jeu.getAvatar().updateConnexion(false);
                FenetreDeJeu.this.timer.stop();
                if (FenetreDeJeu.this.bgm != null) {
                 FenetreDeJeu.this.bgm.stop();
                }
                dispose();
            }
        });
    }

    /**
     * Calcule l'échelle (scale) pour que LOGICAL_W x LOGICAL_H tienne dans le panel,
     * en gardant le ratio. Calcule aussi les offsets pour centrer (letterbox).
     */
    private void computeScaleAndOffsets() {
        int w = panel.getWidth();
        int h = panel.getHeight();

        // Si pas encore affiché (width=0 au tout début)
        if (w <= 0 || h <= 0) {
            return;
        }

        double sx = (double) w / LOGICAL_W;
        double sy = (double) h / LOGICAL_H;
        scale = Math.min(sx, sy);

        int drawW = (int) Math.round(LOGICAL_W * scale);
        int drawH = (int) Math.round(LOGICAL_H * scale);

        offsetX = (w - drawW) / 2;
        offsetY = (h - drawH) / 2;
    }

    // -------- Boucle de jeu --------
    @Override
    public void actionPerformed(ActionEvent e) {

        // 1) Update logique
        this.jeu.miseAJour();

        // 2) Render dans le framebuffer logique 1280x960
        fbG.setTransform(new AffineTransform());  // reset transform
        fbG.clearRect(0, 0, LOGICAL_W, LOGICAL_H);

        this.jeu.rendu(fbG);

        // 3) Affichage (panel dessine framebuffer avec scale)
        this.panel.repaint();
    }

    // -------- Gestion clavier --------
    @Override
    public void keyTyped(KeyEvent evt) { /* inutilisé */ }

    @Override
    public void keyPressed(KeyEvent evt) {
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
                this.jeu.getAvatar().setToucheDroite(true);
                break;
            case KeyEvent.VK_LEFT:
                this.jeu.getAvatar().setToucheGauche(true);
                break;
            case KeyEvent.VK_UP:
                this.jeu.getAvatar().setToucheHaut(true);
                break;
            case KeyEvent.VK_DOWN:
                this.jeu.getAvatar().setToucheBas(true);
                break;
            case KeyEvent.VK_ENTER:
                this.jeu.getAvatar().takeHit();
                break;
            case KeyEvent.VK_SPACE:
                this.jeu.getAvatar().heal();
                break;
            case KeyEvent.VK_P:
                this.jeu.getAvatar().increasePollen();
                break;
            case KeyEvent.VK_ESCAPE:
                // option: quitter fullscreen rapidement
                // dispose();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent evt) {
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
                this.jeu.getAvatar().setToucheDroite(false);
                break;
            case KeyEvent.VK_LEFT:
                this.jeu.getAvatar().setToucheGauche(false);
                break;
            case KeyEvent.VK_UP:
                this.jeu.getAvatar().setToucheHaut(false);
                break;
            case KeyEvent.VK_DOWN:
                this.jeu.getAvatar().setToucheBas(false);
                break;
        }
    }

    public void terminer() {
        this.jeu.getAvatar().updateConnexion(false);
        this.timer.stop();
        if (this.bgm != null) this.bgm.stop();
    }

    // (optionnel) si um dia você precisar converter clique do mouse para coordenada do jogo:
    public double getScale() { return scale; }
    public int getOffsetX() { return offsetX; }
    public int getOffsetY() { return offsetY; }
}
