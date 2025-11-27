/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jeu;

import java.awt.Color;
import java.awt.Graphics2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import outils.SingletonJDBC;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.sql.DriverManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Exemple de classe avatar
 *
 * @author guillaume.laurent
 */
public class Avatar {

    protected BufferedImage sprite;
    private boolean toucheHaut, toucheBas, toucheDroite, toucheGauche;
    private String pseudo;
    private double x = 0;
    private double y = 0;
//    protected Carte laCarte;

    public Avatar() {
//        this.laCarte = laCarte;
        try {
            this.sprite = ImageIO.read(getClass().getResource("../resources/abeille.png"));
        } catch (IOException ex) {
            Logger.getLogger(Avatar_old.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.toucheHaut   = false;
        this.toucheBas    = false;
        this.toucheDroite = false;
        this.toucheGauche = false;
        this.pseudo = "abeille1";
        
        updateConnexion(true);
        
        try {

            Connection connexion = SingletonJDBC.getInstance().getConnection();

            PreparedStatement requete = connexion.prepareStatement("SELECT x, y FROM abeille WHERE pseudo = ?");
            requete.setString(1, this.pseudo);
            ResultSet resultat = requete.executeQuery();
            while (resultat.next()) {
                x = resultat.getDouble("x");
                y = resultat.getDouble("y");
            }

            requete.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    public void miseAJour() {
        
        if (this.toucheHaut) {
            y -= 5;
        }
        if (this.toucheBas) {
            y += 5;
        }
        if (this.toucheDroite) {
            x += 5;
        }
        if (this.toucheGauche) {
            x -= 5;
        }
        this.toucheHaut = false;
        this.toucheBas = false;
        this.toucheDroite = false;
        this.toucheGauche = false;
        
        try {

            Connection connexion = SingletonJDBC.getInstance().getConnection();

            PreparedStatement requete = connexion.prepareStatement("UPDATE abeille SET x = ?, y = ? WHERE pseudo = ?");
            requete.setDouble(1, x);
            requete.setDouble(2, y);
            requete.setString(3, this.pseudo);
            int nombreDeModifications = requete.executeUpdate();
            
            requete.close();
        } 
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void rendu(Graphics2D contexte) {
        try {

            Connection connexion = SingletonJDBC.getInstance().getConnection();

            PreparedStatement requete = connexion.prepareStatement("SELECT x, y FROM abeille WHERE pseudo = ?");
            requete.setString(1, this.pseudo);
            ResultSet resultat = requete.executeQuery();
            while (resultat.next()) {
                x = resultat.getDouble("x");
                y = resultat.getDouble("y");
            }

            requete.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        contexte.drawImage(this.sprite, (int) x, (int) y, null);
//        try {
//            Connection connexion = SingletonJDBC.getInstance().getConnection();
//
//            PreparedStatement requete = connexion.prepareStatement("SELECT latitude, longitude FROM dresseurs WHERE pseudo = ?");
//            requete.setString(1, pseudo);
//            ResultSet resultat = requete.executeQuery();
//            if (resultat.next()) {
//                double latitude = resultat.getDouble("latitude");
//                double longitude = resultat.getDouble("longitude");
//                //System.out.println(pseudo + " = (" + latitude + "; " + longitude + ")");
//
////                int x = laCarte.longitudeEnPixel(longitude);
////                int y = laCarte.latitudeEnPixel(latitude);
////                contexte.setColor(Color.red);
////                contexte.drawOval(x - 7, y - 7, 14, 14);
//                //contexte.drawString(pseudo, x + 8, y - 8);
//            }
//            requete.close();
//
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
    }

    public void setToucheHaut(boolean etat) {
        this.toucheHaut = etat;
    }

    public void setToucheBas(boolean etat) {
        this.toucheBas = etat;
    }

    public void setToucheGauche(boolean etat) {
        this.toucheGauche = etat;
    }

    public void setToucheDroite(boolean etat) {
        this.toucheDroite = etat;
    }
    
    public void updateConnexion(boolean var){
        try {

            Connection connexion = SingletonJDBC.getInstance().getConnection();

            PreparedStatement requete = connexion.prepareStatement("UPDATE abeille SET connecte = ? WHERE pseudo = ?");
            requete.setBoolean(1, var);
            requete.setString(2, this.pseudo);
            int nombreDeModifications = requete.executeUpdate();
            
            requete.close();
        } 
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

}
