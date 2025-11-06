/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jeu;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Exemple de classe lutin
 *
 * @author guillaume.laurent
 */
public class Avatar {

    protected BufferedImage sprite;
    protected double x, y;
    private boolean toucheGauche, toucheDroite, toucheHaut, toucheBas;

    public Avatar() {
        try {
            this.sprite = ImageIO.read(getClass().getResource("../resources/abeille.png"));
        } catch (IOException ex) {
            Logger.getLogger(Avatar.class.getName()).log(Level.SEVERE, null, ex);
        }
        lancer();
        this.toucheGauche = false;
        this.toucheDroite = false;
        this.toucheHaut   = false;
        this.toucheBas    = false;
        int width = sprite.getWidth();
        int height = sprite.getHeight();
    }

    public void miseAJour() {
        if (this.toucheGauche) {
            x -= 5;
        }
        if (this.toucheDroite) {
            x += 5;
        }
        if (x > 920 - sprite.getWidth()) { // collision avec le bord droit de la scene
            x = 920 - sprite.getWidth();
        }
        if (x < 0) { // collision avec le bord gauche de la scene
            x = 0;
        }
        
        if (this.toucheHaut) {
            y -= 5;
        }
        if (this.toucheBas) {
            y += 5;
        }
        if (y > 920 - sprite.getHeight()) { // collision avec le bord bas de la scene
            y = 920 - sprite.getHeight();
        }
        if (y < 0) { // collision avec le bord haut de la scene
            y = 0;
        }
    }

    public void rendu(Graphics2D contexte) {
        contexte.drawImage(this.sprite, (int) x, (int) y, null);
    }

    public void lancer() {
        this.x = 170;
        this.y = 320;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
    
    public double getLargeur() {
        return sprite.getHeight();
    }

    public double getHauteur() {
        return sprite.getWidth();
    }
    
    public void setToucheGauche(boolean etat) {
        this.toucheGauche = etat;
    }
    
    public void setToucheDroite(boolean etat) {
        this.toucheDroite = etat;
    }
    
    public void setToucheHaut(boolean etat) {
        this.toucheHaut = etat;
    }
    
    public void setToucheBas(boolean etat) {
        this.toucheBas = etat;
    }
    
    
}

