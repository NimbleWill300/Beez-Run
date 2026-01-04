/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jeu;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import outils.OutilsJDBC;



/**
 *
 * @author guillaume.laurent
 */
public class InitialisationBDD {

//    public static void main(String[] args) throws IOException {
    public InitialisationBDD() throws IOException{

        try {

            Connection connexion =  DriverManager.getConnection("jdbc:mariadb://nemrod.ens2m.fr:3306/2025-2026_s1_vs1_tp2_abeille", "etudiant", "YTDTvj9TR3CDYCmP");

            Statement statement = connexion.createStatement() ;
            
            ResultSet resultat = statement.executeQuery("SELECT * FROM abeille;");
            OutilsJDBC.afficherResultSet(resultat);
            
            ResultSet resultat2 = statement.executeQuery("SELECT * FROM frelon;");
            OutilsJDBC.afficherResultSet(resultat2);
            
            statement.close();
            connexion.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
//        FenetreDeJeu fenetre = new FenetreDeJeu("abeille4");
//        fenetre.setVisible(true);
        
    }

}
