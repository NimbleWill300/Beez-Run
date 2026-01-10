package jeu;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import outils.SingletonJDBC;

public class Ruche {

    private BufferedImage sprite;

    private int id = -1;      // id da ruche encontrada no banco
    private double x, y;
    private int score;

    // para não consultar o banco a cada frame
    private int tick = 0;
    private final int ticksPerDB = 10; // a cada 10 updates (~0.4s se seu Timer é 40ms)
    
    public static final int W = 128;
    public static final int H = 128;


    public Ruche() throws IOException {
        // coloque a imagem em src/resources/beehive.png
        this.sprite = ImageIO.read(getClass().getResource("/resources/beehive.png"));
        this.x = 0;
        this.y = 0;
        this.score = 0;

        // puxa já na criação
        refreshFromDB();
    }

    public void miseAJour() {
        tick++;
        if (tick < ticksPerDB) return;
        tick = 0;

        refreshFromDB();

        // Se você QUISER realmente "atualizar com frequência" no banco mesmo estática,
        // você poderia re-salvar o que você leu (não muda nada). Eu deixaria desligado por enquanto.
        // pushToDB();
    }

    private void refreshFromDB() {
        try {
            Connection connexion = SingletonJDBC.getInstance().getConnection();

            // Pega a ruche com menor id (primeira criada)
            PreparedStatement st = connexion.prepareStatement(
                "SELECT id, x, y, score FROM ruche ORDER BY id ASC LIMIT 1"
            );

            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                this.id = rs.getInt("id");
                this.x = rs.getDouble("x");
                this.y = rs.getDouble("y");
                this.score = rs.getInt("score");
            }

            rs.close();
            st.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // (Opcional) se no futuro você quiser empurrar score pro DB
    public void pushToDB() {
        if (id < 0) return;

        try {
            Connection connexion = SingletonJDBC.getInstance().getConnection();

            PreparedStatement st = connexion.prepareStatement(
                "UPDATE ruche SET x = ?, y = ?, score = ? WHERE id = ?"
            );
            st.setDouble(1, x);
            st.setDouble(2, y);
            st.setInt(3, score);
            st.setInt(4, id);
            st.executeUpdate();
            st.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void rendu(Graphics2D g) {
        if (sprite != null) {
            g.drawImage(sprite, (int) x, (int) y, null);
        }
    }

    // getters
    public double getX() { return x; }
    public double getY() { return y; }
    public int getScore() { return score; }
    public int getId() { return id; }

    // setter (se você quiser mudar score futuramente)
    public void setScore(int score) { this.score = score; }
}
