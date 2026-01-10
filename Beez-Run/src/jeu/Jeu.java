package jeu;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import outils.SingletonJDBC;

public class Jeu {

    public enum GameState { RUNNING, WIN, LOSE }

    private GameState gameState = GameState.RUNNING;

    private final Carte carte;
    private final Monster uneMonster;
    private final Abeille uneAbeille;
    private final Ruche uneRuche;
    private final HUD hud;

    private final List<Fleur> fleurs;
    private final Avatar uneAvatar;

    private final BufferedImage winImg;
    private final BufferedImage loseImg;

    private static final double FRELON_SPAWN_X = 350;
    private static final double FRELON_SPAWN_Y = 700;

    public Jeu(String name) throws IOException {
        this.carte = new Carte();

        // ✅ Monster agora precisa da Carte (para collisions)
        this.uneMonster = new Monster(this.carte);

        this.uneAbeille = new Abeille();
        this.uneRuche = new Ruche();

        // ✅ 6 flores (id 1..6) usando o MESMO spritesheet (economiza memória)
        this.fleurs = new ArrayList<>();
        SpriteSheetFleur sharedSheet = new SpriteSheetFleur();
        for (int i = 1; i <= 6; i++) {
            this.fleurs.add(new Fleur(i, sharedSheet));
        }

        this.uneAvatar = new Avatar(name, this.carte, this.fleurs, this.uneRuche);

        this.hud = new HUD(this.uneAvatar);

        this.winImg  = ImageIO.read(getClass().getResource("/resources/gamewin.png"));
        this.loseImg = ImageIO.read(getClass().getResource("/resources/gamelost.png"));

        // ✅ força spawn do frelon no DB ao iniciar
        forceFrelonSpawnDB(FRELON_SPAWN_X, FRELON_SPAWN_Y);
    }

    public void miseAJour() {
        if (gameState != GameState.RUNNING) return;

        uneRuche.miseAJour();
        for (Fleur f : fleurs) f.miseAJour();

        uneMonster.miseAJour();
        uneAvatar.miseAJour();
        uneAbeille.miseAJour();

        checkEndGame();
    }

    public void rendu(Graphics2D g) {
        carte.rendu(g);

        // cenário primeiro
        uneRuche.rendu(g);
        for (Fleur f : fleurs) f.rendu(g);

        // entidades móveis por cima
        uneMonster.rendu(g);
        uneAbeille.rendu(g);

        hud.rendu(g);

        if (gameState == GameState.WIN) {
            g.drawImage(winImg, 0, 0, null);
            drawPressSpace(g);
        } else if (gameState == GameState.LOSE) {
            g.drawImage(loseImg, 0, 0, null);
            drawPressSpace(g);
        }
    }

    private void drawPressSpace(Graphics2D g) {
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.setColor(java.awt.Color.BLACK); // ✅ preto
        String msg = "Press SPACE to play again";
        int x = 1280 / 2 - g.getFontMetrics().stringWidth(msg) / 2;
        int y = 960 - 80;
        g.drawString(msg, x, y);
    }

    private void checkEndGame() {
        // 12 entregas * 3 polens = 36 (se foi essa sua regra)
        if (uneRuche.getScore() >= 36) {
            gameState = GameState.WIN;
            return;
        }

        if (todasAbelhasConectadasMortas()) {
            gameState = GameState.LOSE;
        }
    }

    private boolean todasAbelhasConectadasMortas() {
        try {
            Connection c = SingletonJDBC.getInstance().getConnection();
            PreparedStatement st = c.prepareStatement(
                "SELECT COUNT(*) AS vivos FROM abeille WHERE connecte = 1 AND pv > 0"
            );
            ResultSet rs = st.executeQuery();
            int vivos = 0;
            if (rs.next()) vivos = rs.getInt("vivos");
            rs.close();
            st.close();
            return vivos == 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean isFinished() {
        return gameState != GameState.RUNNING;
    }

    public Avatar getAvatar() { return uneAvatar; }

    // ============================================================
    // RESET GAME (SPACE)
    // ============================================================
    public void resetGame() {
        try {
            Connection c = SingletonJDBC.getInstance().getConnection();

            // 1) reset Ruche
            PreparedStatement stR = c.prepareStatement(
                "UPDATE ruche SET score = 0 WHERE id = 1"
            );
            stR.executeUpdate();
            stR.close();

            // 2) reset Flores (etat=1 e next_available_at não nulo)
            PreparedStatement stF = c.prepareStatement(
                "UPDATE fleur SET etat = 1, next_available_at = NOW() WHERE id BETWEEN 1 AND 6"
            );
            stF.executeUpdate();
            stF.close();

            // 3) reset Abelhas perto da colmeia
            double rx = 0, ry = 0;
            PreparedStatement stPos = c.prepareStatement("SELECT x, y FROM ruche WHERE id = 1");
            ResultSet rs = stPos.executeQuery();
            if (rs.next()) {
                rx = rs.getDouble("x");
                ry = rs.getDouble("y");
            }
            rs.close();
            stPos.close();

            PreparedStatement stB = c.prepareStatement(
                "UPDATE abeille " +
                "SET x = CASE pseudo " +
                "  WHEN (SELECT pseudo FROM abeille WHERE connecte=1 ORDER BY pseudo LIMIT 1 OFFSET 0) THEN ? " +
                "  WHEN (SELECT pseudo FROM abeille WHERE connecte=1 ORDER BY pseudo LIMIT 1 OFFSET 1) THEN ? " +
                "  WHEN (SELECT pseudo FROM abeille WHERE connecte=1 ORDER BY pseudo LIMIT 1 OFFSET 2) THEN ? " +
                "  WHEN (SELECT pseudo FROM abeille WHERE connecte=1 ORDER BY pseudo LIMIT 1 OFFSET 3) THEN ? " +
                "  ELSE x END, " +
                "y = CASE pseudo " +
                "  WHEN (SELECT pseudo FROM abeille WHERE connecte=1 ORDER BY pseudo LIMIT 1 OFFSET 0) THEN ? " +
                "  WHEN (SELECT pseudo FROM abeille WHERE connecte=1 ORDER BY pseudo LIMIT 1 OFFSET 1) THEN ? " +
                "  WHEN (SELECT pseudo FROM abeille WHERE connecte=1 ORDER BY pseudo LIMIT 1 OFFSET 2) THEN ? " +
                "  WHEN (SELECT pseudo FROM abeille WHERE connecte=1 ORDER BY pseudo LIMIT 1 OFFSET 3) THEN ? " +
                "  ELSE y END, " +
                "qnt_pollen = 0, etat = 0, pv = 5 " +
                "WHERE connecte = 1"
            );

            // offsets X
            stB.setDouble(1, rx + 20);
            stB.setDouble(2, rx + 80);
            stB.setDouble(3, rx + 20);
            stB.setDouble(4, rx + 80);

            // offsets Y
            stB.setDouble(5, ry + 40);
            stB.setDouble(6, ry + 40);
            stB.setDouble(7, ry + 100);
            stB.setDouble(8, ry + 100);

            stB.executeUpdate();
            stB.close();

            // 4) reset Frelon
            forceFrelonSpawnDB(FRELON_SPAWN_X, FRELON_SPAWN_Y);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        gameState = GameState.RUNNING;

        // força refresh local
        uneRuche.miseAJour();
        for (Fleur f : fleurs) f.miseAJour();
    }

    // ============================================================
    // HELPER: spawn do frelon via DB
    // ============================================================
    private void forceFrelonSpawnDB(double x, double y) {
        try {
            Connection c = SingletonJDBC.getInstance().getConnection();
            PreparedStatement stM = c.prepareStatement(
                "UPDATE frelon SET x = ?, y = ?, etat = 0 WHERE nom = ?"
            );
            stM.setDouble(1, x);
            stM.setDouble(2, y);
            stM.setString(3, "frelon2");
            stM.executeUpdate();
            stM.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
