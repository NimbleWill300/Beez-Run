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
public final class Avatar {

    protected BufferedImage sprite;
    private boolean toucheHaut, toucheBas, toucheDroite, toucheGauche, faceDroite;
    private String pseudo;
    private double x = 0;
    private double y = 0;
    private int vitesse = 10;
    private int pv = 20;
    private int pollen = 0;
    private int etat = 0;
    private int damageDelay = 0;
    private int delay = 15;
    
//    protected Carte laCarte;

    public Avatar(String name) {
//        this.laCarte = laCarte;
        try {
            this.sprite = ImageIO.read(getClass().getResource("../resources/abeille.png"));
        } catch (IOException ex) {
            Logger.getLogger(Avatar.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.toucheHaut   = false;
        this.toucheBas    = false;
        this.toucheDroite = false;
        this.toucheGauche = false;
        this.pseudo = name;
        
        updateConnexion(true);
        
        try {

            Connection connexion = SingletonJDBC.getInstance().getConnection();

            PreparedStatement requete = connexion.prepareStatement("SELECT x, y, pv, qnt_pollen, etat FROM abeille WHERE pseudo = ?");
            requete.setString(1, this.pseudo);
            ResultSet resultat = requete.executeQuery();
            while (resultat.next()) {
                this.x = resultat.getDouble("x");
                this.y = resultat.getDouble("y");
                this.pv = resultat.getInt("pv");
                this.pollen = resultat.getInt("qnt_pollen");
                this.etat = resultat.getInt("etat");
            }

            requete.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    public void miseAJour() {

        // Get avatar's informations from database
        try {
                Connection connexion = SingletonJDBC.getInstance().getConnection();

                PreparedStatement requete = connexion.prepareStatement("SELECT pv, qnt_pollen, etat FROM abeille WHERE pseudo = ?");
                requete.setString(1, this.pseudo);
                ResultSet resultat = requete.executeQuery();
                while (resultat.next()) {
                    this.pv     = resultat.getInt("pv");
                    this.pollen = resultat.getInt("qnt_pollen");
                    this.etat = resultat.getInt("etat");
                }

                requete.close();

            } catch (SQLException ex) {
                ex.printStackTrace();
        }
        
        if(pv > 0){
            if(damageDelay > 0){
                etat = 4;
                damageDelay -= 1;
            }else{
                etat = pollen;
            }
            // Update avatar's position
            if (this.toucheHaut) {
                this.y -= vitesse;
            }
            if (this.toucheBas) {
                this.y += vitesse;
            }
            if (this.toucheDroite) {
                this.x += vitesse;
                faceDroite = true;
            }
            if (this.toucheGauche) {
                this.x -= vitesse;
                faceDroite = false;
            }
            this.toucheHaut = false;
            this.toucheBas = false;
            this.toucheDroite = false;
            this.toucheGauche = false;
        }else{
            etat = 5;
            damageDelay = 0;
            this.toucheHaut = false;
            this.toucheBas = false;
            this.toucheDroite = false;
            this.toucheGauche = false;
        }

            // Send new avatar's information to database
            try {

                Connection connexion = SingletonJDBC.getInstance().getConnection();

                PreparedStatement requete = connexion.prepareStatement("UPDATE abeille SET x = ?, y = ?, direction = ?, etat = ?, qnt_pollen = ? WHERE pseudo = ?");
                requete.setDouble(1, x);
                requete.setDouble(2, y);
                requete.setBoolean(3, faceDroite);
                requete.setInt(4, this.etat);
                requete.setInt(5, this.pollen);
                requete.setString(6, this.pseudo);
                int nombreDeModifications = requete.executeUpdate();

                requete.close();
            } 
            catch (SQLException ex) {
                ex.printStackTrace();
            }
            
    }

    public void rendu(Graphics2D contexte) {
        // Abeille rendering made in Abeille.java
    }

    public boolean getDirection(){
        return faceDroite;
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
    
    public int getPv(){
        return this.pv;
    }

    public int getPollen(){
        return this.pollen;
    }
    
    public void increasePollen(){
        pollen = (pollen + 1) % 4;
        try {
            Connection connexion = SingletonJDBC.getInstance().getConnection();

            PreparedStatement requete = connexion.prepareStatement("UPDATE abeille SET qnt_pollen = ? WHERE pseudo = ?");
            requete.setInt(1, pollen);
            requete.setString(2, this.pseudo);
            int nombreDeModifications = requete.executeUpdate();
            
            requete.close();
        } 
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    public void takeHit(){
        if(etat < 4){
            pv -= 1;
            try {
                Connection connexion = SingletonJDBC.getInstance().getConnection();

                PreparedStatement requete = connexion.prepareStatement("UPDATE abeille SET pv = ? WHERE pseudo = ?");
                requete.setInt(1, pv);
                requete.setString(2, this.pseudo);
                int nombreDeModifications = requete.executeUpdate();

                requete.close();
            } 
            catch (SQLException ex) {
                ex.printStackTrace();
            }
            if(pv > 0){
                damageDelay = delay;
            }
            try {
                Connection connexion = SingletonJDBC.getInstance().getConnection();

                PreparedStatement requete = connexion.prepareStatement("UPDATE abeille SET etat = ? WHERE pseudo = ?");
                requete.setInt(1, etat);
                requete.setString(2, this.pseudo);
                int nombreDeModifications = requete.executeUpdate();

                requete.close();
            } 
            catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public void heal(){
        if(pv < 5){
            pv += 1;
            try {
                Connection connexion = SingletonJDBC.getInstance().getConnection();

                PreparedStatement requete = connexion.prepareStatement("UPDATE abeille SET pv = ? WHERE pseudo = ?");
                requete.setInt(1, pv);
                requete.setString(2, this.pseudo);
                int nombreDeModifications = requete.executeUpdate();

                requete.close();
            } 
            catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
}
