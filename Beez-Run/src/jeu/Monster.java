/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jeu;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import outils.SingletonJDBC;

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
    protected int vitesse = 3;
    

    public Monster() {
        try {
            this.sprite = ImageIO.read(getClass().getResource("../resources/ferlon.png"));
        } catch (IOException ex) {
            Logger.getLogger(Monster.class.getName()).log(Level.SEVERE, null, ex);
        }
        lancer();
    }

    public void miseAJour() {
        
        try {

            Connection connexion = SingletonJDBC.getInstance().getConnection();

            PreparedStatement requete = connexion.prepareStatement("SELECT x, y FROM frelon WHERE nom = ?");
            requete.setString(1, "frelon2");
            ResultSet resultat = requete.executeQuery();
            while (resultat.next()) {
                x = resultat.getDouble("x");
                y = resultat.getDouble("y");
            }

            requete.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
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
        try {

            Connection connexion = SingletonJDBC.getInstance().getConnection();

            PreparedStatement requete = connexion.prepareStatement("UPDATE frelon SET x = ?, y = ? WHERE nom = ?");
            requete.setDouble(1, x);
            requete.setDouble(2, y);
            requete.setString(3, "frelon2");
            int nombreDeModifications = requete.executeUpdate();

            requete.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
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

