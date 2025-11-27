/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jeu;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author eschroed
 */
public class Jeu {
    private BufferedImage decor;
    private int score;
    private Monster uneMonster;
    private Avatar uneAvatar;
    private Dresseurs uneDresseurs;
    
    public Jeu() {
        try {
            this.decor = ImageIO.read(getClass().getResource("../resources/background_jeu.png"));
        } catch (IOException ex) {
            Logger.getLogger(Jeu.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.score = 0;
        this.uneMonster = new Monster();
        this.uneAvatar = new Avatar();
        this.uneDresseurs = new Dresseurs();
        System.out.print("Hello World");

    }

    public void rendu(Graphics2D contexte) {
        contexte.drawImage(this.decor, 0, 0, null);
        contexte.drawString("Score : " + score, 10, 20);
        this.uneMonster.rendu(contexte);
//        this.uneAvatar.rendu(contexte);
        this.uneDresseurs.rendu(contexte);
    }
    public void miseAJour() {
        this.uneMonster.miseAJour();
        this.uneAvatar.miseAJour();
        this.uneDresseurs.miseAJour();
        // 1. Mise à jour de l’avatar en fonction des commandes des joueurs
        // 2. Mise à jour des autres éléments (objets, monstres, etc.)
        // 3. Gérer les interactions (collisions et autres règles)
    }
//    public boolean estTermine() {
//        // Renvoie vrai si la partie est terminée (gagnée ou perdue)
//        return this.uneMonster.getY()>400;
//    }
    
    public Avatar getAvatar(){
        return uneAvatar;
    }
}
