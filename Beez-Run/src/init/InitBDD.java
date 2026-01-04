/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;

public class InitBDD {

    public static void main(String[] args) {
        String url = "jdbc:mariadb://nemrod.ens2m.fr:3306/2025-2026_s1_vs1_tp2_abeille";
        String user = "etudiant";
        String pass = "YTDTvj9TR3CDYCmP";

        String createTableSql = ""
            + "CREATE TABLE IF NOT EXISTS utilisateurs ("
            + "  pseudo VARCHAR(50) PRIMARY KEY,"
            + "  motdepasse CHAR(32) NOT NULL"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        String insertTestUser = ""
            + "INSERT INTO utilisateurs (pseudo, motdepasse) "
            + "SELECT 'test', MD5('12345') "
            + "FROM DUAL "
            + "WHERE NOT EXISTS (SELECT 1 FROM utilisateurs WHERE pseudo='test');";

        try (Connection c = DriverManager.getConnection(url, user, pass);
             Statement st = c.createStatement()) {

            System.out.println("Connexion OK -> création de la table si nécessaire...");
            st.executeUpdate(createTableSql);
            System.out.println("Table 'utilisateurs' créée (ou déjà existante).");

            System.out.println("Insertion d'un compte test si absent...");
            int n = st.executeUpdate(insertTestUser);
            if (n > 0) {
                System.out.println("Compte test ajouté (pseudo='test', mdp='12345').");
            } else {
                System.out.println("Compte test déjà présent (pseudo='test').");
            }

            // Vérification rapide : lister les pseudos
            try (ResultSet rs = st.executeQuery("SELECT pseudo FROM utilisateurs LIMIT 10")) {
                System.out.println("Exemple de pseudos dans la table :");
                while (rs.next()) {
                    System.out.println(" - " + rs.getString("pseudo"));
                }
            }

            System.out.println("Initialisation terminée.");

        } catch (Exception ex) {
            System.err.println("Erreur : " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
