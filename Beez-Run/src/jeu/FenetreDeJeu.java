package jeu;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

/**
 * Fenêtre principale du jeu
 * Rendu en framebuffer + boucle de jeu via Timer
 *
 * Carte : 1280 x 960
 * Tile   : 64 x 64
 *
 * @author guillaume.laurent
 */
public class FenetreDeJeu extends JFrame implements ActionListener, KeyListener {

    private BufferedImage framebuffer;
    private Graphics2D contexte;
    private JLabel jLabel1;
    private Jeu jeu;
    private Timer timer;

    public FenetreDeJeu(String name) throws IOException {

        // -------- Création du JLabel AVANT toute utilisation --------
        this.jLabel1 = new JLabel();
        this.jLabel1.setPreferredSize(new java.awt.Dimension(1280, 960));

        // -------- Configuration de la fenêtre --------
        this.setTitle("Beez Run");
        this.setResizable(false);
        this.setContentPane(this.jLabel1);
        this.pack();
        this.setLocationRelativeTo(null); // centre l'écran
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // -------- Création du framebuffer --------
        this.framebuffer = new BufferedImage(
                1280,
                960,
                BufferedImage.TYPE_INT_ARGB
        );
        this.jLabel1.setIcon(new ImageIcon(framebuffer));
        this.contexte = this.framebuffer.createGraphics();

        // -------- Création du jeu --------
        this.jeu = new Jeu(name);

        // -------- Timer (boucle de jeu ~25 FPS) --------
        this.timer = new Timer(40, this);
        this.timer.start();

        // -------- Input clavier --------
        this.addKeyListener(this);
        this.setFocusable(true);
        this.requestFocusInWindow();

        // -------- Fermeture propre --------
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Fermeture de la fenêtre...");

                // Déconnexion propre de l'avatar
                FenetreDeJeu.this.jeu.getAvatar().updateConnexion(false);

                // Stoppe le timer
                FenetreDeJeu.this.timer.stop();

                // Ferme la fenêtre
                dispose();
            }
        });
    }

    // -------- Boucle de jeu --------
    @Override
    public void actionPerformed(ActionEvent e) {
        this.jeu.miseAJour();
        this.jeu.rendu(contexte);
        this.jLabel1.repaint();
    }

    // -------- Gestion clavier --------
    @Override
    public void keyTyped(KeyEvent evt) {
        // inutilisé
    }

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
    }
}
