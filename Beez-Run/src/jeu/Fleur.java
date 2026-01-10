package jeu;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.*;
import outils.SingletonJDBC;

public class Fleur {

    private final int id;                 
    private final SpriteSheetFleur sheet;

    private BufferedImage sprite;
    private double x, y;

    // etat: 1 = com polen (colorida), 0 = sem polen (cinza)
    private int etat = 1;

    private Timestamp nextAvailableAt; // pode ser null

    // não bater no DB todo frame
    private int tick = 0;
    private final int ticksPerDB = 5; // ~200ms (Timer 40ms)

    // evita spam de UPDATE quando a abelha fica em cima
    private long lastCollectAttemptMs = 0;
    private static final long COLLECT_COOLDOWN_MS = 250;

    // tamanho real do sprite/hitbox da flor
    public static final int W = 57;
    public static final int H = 57;

    public Fleur(int id, SpriteSheetFleur sharedSheet) throws IOException {
        this.id = id;
        this.sheet = sharedSheet;           
        this.sprite = sheet.getFrame(0);    

        refreshFromDB();                    
        updateSprite();
    }

    public void miseAJour() {
        tick++;
        if (tick < ticksPerDB) return;
        tick = 0;

        refreshFromDB();
        applyRespawnIfNeeded(); // respawn feito pelo DB (NOW)
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
            if (resultat.next()) {
                this.x = resultat.getDouble("x");
                this.y = resultat.getDouble("y");
                this.etat = resultat.getInt("etat");
                this.nextAvailableAt = resultat.getTimestamp("next_available_at");
            }
            resultat.close();
            requete.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Se etat==0 e NOW() >= next_available_at, volta etat=1 no BANCO.
     */
    private void applyRespawnIfNeeded() {
        if (etat != 0) return;

        try {
            Connection c = SingletonJDBC.getInstance().getConnection();

            PreparedStatement st = c.prepareStatement(
<<<<<<< Updated upstream
                "UPDATE fleur " +
                "SET etat = 1, next_available_at = NULL " +
                "WHERE id = ? AND etat = 0 AND next_available_at IS NOT NULL AND NOW() >= next_available_at"
            );
            st.setInt(1, id);

            int updated = st.executeUpdate();
            st.close();

            if (updated > 0) {
                this.etat = 1;
                this.nextAvailableAt = null;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void updateSprite() {
        this.sprite = (etat == 1) ? sheet.getFrame(0) : sheet.getFrame(1);
    }

    /**
     * UPDATE atômico: só 1 jogador consegue coletar.
     */
    public boolean tryCollect() {
        long nowMs = System.currentTimeMillis();
        if (nowMs - lastCollectAttemptMs < COLLECT_COOLDOWN_MS) return false;
        lastCollectAttemptMs = nowMs;

        if (etat != 1) return false;

        try {
            Connection c = SingletonJDBC.getInstance().getConnection();

            PreparedStatement st = c.prepareStatement(
                "UPDATE fleur " +
                "SET etat = 0, next_available_at = NOW() + INTERVAL 10 SECOND " +
                "WHERE id = ? AND etat = 1"
            );
            st.setInt(1, id);

            int updated = st.executeUpdate();
            st.close();

            if (updated > 0) {
                this.etat = 0;
                return true;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public void rendu(Graphics2D g) {
        g.drawImage(sprite, (int) x, (int) y, null);
    }

    // getters
    public int getId() { return id; }
    public double getX() { return x; }
    public double getY() { return y; }
    public int getEtat() { return etat; }
}
