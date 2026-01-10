package jeu;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.*;
import java.time.Instant;
import java.util.Objects;
import outils.SingletonJDBC;

public class Fleur {

    private final int id = 1; // id da flor no banco
    private final SpriteSheetFleur sheet;

    private BufferedImage sprite;
    private double x, y;
    private int score;
    private int etat;

    private Timestamp nextAvailableAt; // pode ser null

    // não bater no DB todo frame
    private int tick = 0;
    private final int ticksPerDB = 5; // ~200ms (Timer 40ms)

    public Fleur() throws IOException {
        this.sheet = new SpriteSheetFleur();
        this.sprite = sheet.getFrame(0);
    }

    public void miseAJour() {
        tick++;
        if (tick < ticksPerDB) return;
        tick = 0;

        refreshFromDB();
        applyRespawnIfNeeded();
        updateSprite();
    }

    private void refreshFromDB() {
       try {
            Connection connexion = SingletonJDBC.getInstance().getConnection();
            PreparedStatement requete = connexion.prepareStatement(
                    "SELECT x, y, etat, next_available_at FROM fleur WHERE id = ?"
            );
            requete.setInt(1, this.id);

            ResultSet resultat = requete.executeQuery();
            while (resultat.next()) {
                this.x = resultat.getInt("x");
                this.y = resultat.getInt("y");
                this.etat = resultat.getInt("etat");
                this.nextAvailableAt = resultat.getTimestamp("next_available_at");
            }
            requete.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Se score==0 e já passou do next_available_at, volta a flor para score=1 no BANCO.
     * (isso garante que todos os PCs veem o mesmo estado)
     */
    private void applyRespawnIfNeeded() {
        if (score > 0) return;
        if (nextAvailableAt == null) return;

        Timestamp now = Timestamp.from(Instant.now());
        if (now.before(nextAvailableAt)) return;

        // respawn no banco: score=1 e next_available_at = NULL
        try {
            Connection c = SingletonJDBC.getInstance().getConnection();
            PreparedStatement st = c.prepareStatement(
                "UPDATE fleur SET score = 1, next_available_at = NULL WHERE id = ?"
            );
            st.setInt(1, id);
            st.executeUpdate();
            st.close();

            // reflete localmente também
            this.score = 1;
            this.nextAvailableAt = null;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void updateSprite() {
        // score 1 => colorida, score 0 => cinza
        this.sprite = (score > 0) ? sheet.getFrame(0) : sheet.getFrame(1);
    }

    public void rendu(Graphics2D g) {
        g.drawImage(sprite, (int) x, (int) y, null);
    }

    // getters úteis depois (interação)
    public double getX() { return x; }
    public double getY() { return y; }
    public int getScore() { return score; }
}
