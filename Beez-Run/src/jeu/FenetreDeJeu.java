package jeu;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.EventListener;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;


/**
 * Exemple de fenetre de jeu en utilisant uniquement des commandes
 *
 * @author guillaume.laurent
 */
public class FenetreDeJeu extends JFrame implements ActionListener, KeyListener {
    
    private BufferedImage framebuffer;
    private Graphics2D contexte;
    private JLabel jLabel1;
    private Jeu jeu;
    private Timer timer;
    private KeyEvent evt;
    
    public FenetreDeJeu(String name) throws IOException {
        // initialisation de la fenetre
        this.setSize(907, 913);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.jLabel1 = new JLabel();
        this.jLabel1.setPreferredSize(new java.awt.Dimension(907, 913));
        this.setContentPane(this.jLabel1);
        this.pack();
        // Creation du buffer pour l’affichage du jeu et recuperation du contexte graphique
        this.framebuffer = new BufferedImage(this.jLabel1.getWidth(), this.jLabel1.getHeight(), BufferedImage.
        TYPE_INT_ARGB);
        this.jLabel1.setIcon(new ImageIcon(framebuffer));
        this.contexte = this.framebuffer.createGraphics();
        // Creation du jeu
        this.jeu = new Jeu(name);
        // Creation du Timer qui appelle this.actionPerformed() tous les 40 ms
        this.timer = new Timer(40, this);
        this.timer.start();
        this.addKeyListener(this);
        
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Fenêtre en train de se fermer...");

                // Marca o avatar como desconectado
                FenetreDeJeu.this.jeu.getAvatar().updateConnexion(false);

                // Termina o jogo
                FenetreDeJeu.this.terminer();

                System.out.println("bye bye World");

                // Fecha de verdade a janela / app
                dispose();       // fecha só a janela
                // ou: System.exit(0); se quiser matar o programa todo
            }
        });
    }
    
    // Methode appelee par le timer et qui effectue la boucle de jeu
    @Override
    public void actionPerformed(ActionEvent e) {
        this.jeu.miseAJour();
        this.jeu.rendu(contexte);
        this.jLabel1.repaint();
//        if (this.jeu.estTermine()) {
//            this.timer.stop();
//        }
    }
    
//    public static void main(String[] args) {
//            }

    @Override
    public void keyTyped(KeyEvent evt) {
    }

    @Override
    public void keyPressed(KeyEvent evt) {
        if (evt.getKeyCode() == evt.VK_RIGHT) {
            this.jeu.getAvatar().setToucheDroite(true);   
        }
        if (evt.getKeyCode() == evt.VK_LEFT) {
            this.jeu.getAvatar().setToucheGauche(true);
        }
        if (evt.getKeyCode() == evt.VK_UP) {
            this.jeu.getAvatar().setToucheHaut(true);   
        }
        if (evt.getKeyCode() == evt.VK_DOWN) {
            this.jeu.getAvatar().setToucheBas(true);
        }

    }

    @Override
    public void keyReleased(KeyEvent evt) {
        if (evt.getKeyCode() == evt.VK_RIGHT) {
            this.jeu.getAvatar().setToucheDroite(false);
        }
        if (evt.getKeyCode() == evt.VK_LEFT) {
            this.jeu.getAvatar().setToucheGauche(false);
        }
        if (evt.getKeyCode() == evt.VK_UP) {
            this.jeu.getAvatar().setToucheHaut(false);   
        }
        if (evt.getKeyCode() == evt.VK_DOWN) {
            this.jeu.getAvatar().setToucheBas(false);
        }

    }
    
    public void terminer(){
        this.jeu.getAvatar().updateConnexion(false);
    }
}
