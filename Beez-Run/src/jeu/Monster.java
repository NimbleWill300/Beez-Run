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
public class Monster{

    protected BufferedImage sprite;
    protected double x, y;
    protected int stage = 0;
    protected int squareSizeMax = 400;
    protected int squareSizeMin = 50;
    protected int vitesse = 5;
    

    public Monster() {
        try {
            this.sprite = ImageIO.read(getClass().getResource("../resources/ferlon.png"));
        } catch (IOException ex) {
            Logger.getLogger(Monster.class.getName()).log(Level.SEVERE, null, ex);
        }
        lancer();
    }

    public void miseAJour() {
        if(y <= squareSizeMin && x < squareSizeMax ){
            x = x + vitesse;
        }
        else if(x >= squareSizeMax && y < squareSizeMax){
            y = y + vitesse;
        }
        else if(y >= squareSizeMax && x > squareSizeMin){
            x = x - vitesse;
        }
        else if(x <= squareSizeMin && y > squareSizeMin){
            y = y - vitesse;
        }
    }

    public void rendu(Graphics2D contexte) {
        contexte.drawImage(this.sprite, (int) x, (int) y, null);
    }

    public void lancer() {
        this.x = 50;
        this.y = 50;
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

}

