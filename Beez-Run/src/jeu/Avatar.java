package jeu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import outils.SingletonJDBC;
import java.util.List;

public final class Avatar {

    // -------- Input --------
    private boolean toucheHaut, toucheBas, toucheDroite, toucheGauche;
    private boolean faceDroite;

    // -------- Identité --------
    private final String pseudo;

    // -------- Position (pixels) --------
    private double x = 0;
    private double y = 0;

    // -------- Stats --------
    private int vitesse = 10;
    private int pv = 20;
    private int pollen = 0;
    private int etat = 0;

    // -------- Dégâts --------
    private int damageDelay = 0;
    private final int delay = 15;

    // -------- Références --------
    private final Carte carte;
    private final List<Fleur> fleurs;
    private final Ruche ruche; // ✅ colmeia

    // -------- Hitbox --------
    private static final int HIT_W = 32;
    private static final int HIT_H = 32;
    private static final int HIT_OX = 16;
    private static final int HIT_OY = 30;

    // -------- Cooldown depósito --------
    private long lastDepositAttemptMs = 0;
    private static final long DEPOSIT_COOLDOWN_MS = 350;

    public Avatar(String name, Carte carte, List<Fleur> fleurs, Ruche ruche) {
        this.carte = carte;
        this.fleurs = fleurs;
        this.ruche = ruche;
        this.pseudo = name;

        updateConnexion(true);

        // -------- Charge état depuis la DB --------
        try {
            Connection connexion = SingletonJDBC.getInstance().getConnection();
            PreparedStatement requete = connexion.prepareStatement(
                    "SELECT x, y, pv, qnt_pollen, etat FROM abeille WHERE pseudo = ?"
            );
            requete.setString(1, this.pseudo);
            ResultSet resultat = requete.executeQuery();
            while (resultat.next()) {
                this.x = resultat.getDouble("x");
                this.y = resultat.getDouble("y");
                this.pv = resultat.getInt("pv");
                this.pollen = resultat.getInt("qnt_pollen");
                this.etat = resultat.getInt("etat");
            }
            resultat.close();
            requete.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // empêche de spawn dans un mur
        resolveIfSpawnInWall();
    }

    // ============================================================
    // UPDATE
    // ============================================================
    public void miseAJour() {
        int last_etat = etat;

        // -------- Lit pv/pollen/etat du DB (interaction frelon etc.) --------
        try {
            Connection connexion = SingletonJDBC.getInstance().getConnection();
            PreparedStatement requete = connexion.prepareStatement(
                    "SELECT pv, qnt_pollen, etat FROM abeille WHERE pseudo = ?"
            );
            requete.setString(1, this.pseudo);

            ResultSet resultat = requete.executeQuery();
            while (resultat.next()) {
                this.pv = resultat.getInt("pv");
                this.pollen = resultat.getInt("qnt_pollen");
                this.etat = resultat.getInt("etat");
            }
            resultat.close();
            requete.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (etat == 4 && last_etat < 4) {
            takeHit();
        }

        if (pv > 0) {
            if (damageDelay > 0) {
                etat = 4;
                damageDelay -= 1;
            } else {
                etat = pollen;
            }

            // ============================================================
            // Mouvement + collisions (tilemap)
            // ============================================================
            double nx = x;
            double ny = y;

            if (toucheHaut) ny -= vitesse;
            if (toucheBas)  ny += vitesse;
            if (toucheDroite) { nx += vitesse; faceDroite = true; }
            if (toucheGauche) { nx -= vitesse; faceDroite = false; }

            // Déplacement séparé (slide)
            // X
            if (!collidesAt(nx, y)) {
                x = nx;
            } else {
                x = pushOutX(x, nx, y);
            }

            // Y
            if (!collidesAt(x, ny)) {
                y = ny;
            } else {
                y = pushOutY(y, ny, x);
            }

            // ✅ Interaction com flor (depois do movimento)
            checkFlowerPickup();

            // ✅ Entrega na colmeia (depois do movimento)
            checkHiveDeposit();

            // reset (comme ton original)
            toucheHaut = false;
            toucheBas = false;
            toucheDroite = false;
            toucheGauche = false;

        } else {
            etat = 5;
            damageDelay = 0;
            toucheHaut = false;
            toucheBas = false;
            toucheDroite = false;
            toucheGauche = false;
        }

        // -------- Envoie position au DB --------
        try {
            Connection connexion = SingletonJDBC.getInstance().getConnection();
            PreparedStatement requete = connexion.prepareStatement(
                    "UPDATE abeille SET x = ?, y = ?, direction = ?, etat = ?, qnt_pollen = ? WHERE pseudo = ?"
            );
            requete.setDouble(1, x);
            requete.setDouble(2, y);
            requete.setBoolean(3, faceDroite);
            requete.setInt(4, this.etat);
            requete.setInt(5, this.pollen);
            requete.setString(6, this.pseudo);
            requete.executeUpdate();
            requete.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // ============================================================
    // Interaction Fleur (pickup)
    // ============================================================
    private void checkFlowerPickup() {
        if (fleurs == null) return;
        if (etat == 5) return; // morto
        if (pollen == 3) return;

        double ax1 = x + HIT_OX;
        double ay1 = y + HIT_OY;
        double ax2 = ax1 + HIT_W;
        double ay2 = ay1 + HIT_H;

        for (Fleur f : fleurs) {
            if (f == null) continue;
            if (f.getEtat() != 1) continue; // sem polen

            double fx1 = f.getX();
            double fy1 = f.getY();
            double fw = Fleur.W; // ✅ precisa existir em Fleur
            double fh = Fleur.H; // ✅ precisa existir em Fleur

            boolean overlap = ax1 < fx1 + fw && ax2 > fx1 && ay1 < fy1 + fh && ay2 > fy1;
            if (!overlap) continue;

            if (f.tryCollect()) {
                pollen = Math.min(3, pollen + 1);
                etat = pollen;

                try {
                    Connection connexion = SingletonJDBC.getInstance().getConnection();
                    PreparedStatement requete = connexion.prepareStatement(
                            "UPDATE abeille SET qnt_pollen = ? WHERE pseudo = ?"
                    );
                    requete.setInt(1, pollen);
                    requete.setString(2, pseudo);
                    requete.executeUpdate();
                    requete.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                return; // só 1 flor por frame
            }
        }
    }

    // ============================================================
    // Interaction Ruche (deposit)
    // ============================================================
    private void checkHiveDeposit() {
        if (ruche == null) return;
        if (etat == 5) return;     // morto
        if (pollen != 3) return;   // só entrega com 3

        long nowMs = System.currentTimeMillis();
        if (nowMs - lastDepositAttemptMs < DEPOSIT_COOLDOWN_MS) return;
        lastDepositAttemptMs = nowMs;

        // hitbox do avatar
        double ax1 = x + HIT_OX;
        double ay1 = y + HIT_OY;
        double ax2 = ax1 + HIT_W;
        double ay2 = ay1 + HIT_H;

        // hitbox da ruche
        double hx1 = ruche.getX();
        double hy1 = ruche.getY();
        double hw = Ruche.W; // ✅ precisa existir em Ruche
        double hh = Ruche.H; // ✅ precisa existir em Ruche

        boolean overlap = ax1 < hx1 + hw && ax2 > hx1 && ay1 < hy1 + hh && ay2 > hy1;
        if (!overlap) return;

        // ✅ operação atômica no DB: incrementa score e zera pólen do jogador
        Connection c = null;
        try {
            c = SingletonJDBC.getInstance().getConnection();
            c.setAutoCommit(false);

            // 1) Tenta zerar pollen APENAS se ainda estiver 3 (evita double deposit)
            PreparedStatement stBee = c.prepareStatement(
                    "UPDATE abeille SET qnt_pollen = 0 WHERE pseudo = ? AND qnt_pollen = 3"
            );
            stBee.setString(1, pseudo);
            int beeUpdated = stBee.executeUpdate();
            stBee.close();

            if (beeUpdated == 0) {
                c.rollback();
                c.setAutoCommit(true);
                return;
            }

            // 2) Soma no score da colmeia (id = 1)
            PreparedStatement stHive = c.prepareStatement(
                    "UPDATE ruche SET score = score + 3 WHERE id = 1"
            );
            stHive.executeUpdate();
            stHive.close();

            c.commit();
            c.setAutoCommit(true);

            // atualiza local
            pollen = 0;
            etat = pollen;

        } catch (SQLException ex) {
            ex.printStackTrace();
            if (c != null) {
                try {
                    c.rollback();
                    c.setAutoCommit(true);
                } catch (SQLException ignored) {}
            }
        }
    }

    // ============================================================
    // COLLISIONS (4 coins hitbox)
    // ============================================================
    private boolean collidesAt(double newX, double newY) {
        double left   = newX + HIT_OX;
        double right  = newX + HIT_OX + HIT_W - 1;
        double top    = newY + HIT_OY;
        double bottom = newY + HIT_OY + HIT_H - 1;

        return carte.isSolidPixel(left, top)
                || carte.isSolidPixel(right, top)
                || carte.isSolidPixel(left, bottom)
                || carte.isSolidPixel(right, bottom);
    }

    private double pushOutX(double oldX, double targetX, double yFixed) {
        double step = (targetX > oldX) ? 1 : -1;
        double xTest = oldX;
        while (xTest != targetX) {
            double next = xTest + step;
            if (collidesAt(next, yFixed)) {
                return xTest;
            }
            xTest = next;
            if (Math.abs(xTest - targetX) < 0.5) break;
        }
        return xTest;
    }

    private double pushOutY(double oldY, double targetY, double xFixed) {
        double step = (targetY > oldY) ? 1 : -1;
        double yTest = oldY;
        while (yTest != targetY) {
            double next = yTest + step;
            if (collidesAt(xFixed, next)) {
                return yTest;
            }
            yTest = next;
            if (Math.abs(yTest - targetY) < 0.5) break;
        }
        return yTest;
    }

    private void resolveIfSpawnInWall() {
        if (!collidesAt(x, y)) return;

        for (int r = 0; r < 10; r++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dx = -r; dx <= r; dx++) {
                    double nx = x + dx * 5;
                    double ny = y + dy * 5;
                    if (!collidesAt(nx, ny)) {
                        x = nx;
                        y = ny;
                        return;
                    }
                }
            }
        }
    }

    // ============================================================
    // GETTERS / INPUT
    // ============================================================
    public boolean getDirection() { return faceDroite; }

    public void setToucheHaut(boolean etat) { this.toucheHaut = etat; }
    public void setToucheBas(boolean etat) { this.toucheBas = etat; }
    public void setToucheGauche(boolean etat) { this.toucheGauche = etat; }
    public void setToucheDroite(boolean etat) { this.toucheDroite = etat; }

    // ============================================================
    // DB / GAMEPLAY
    // ============================================================
    public void updateConnexion(boolean var) {
        try {
            Connection connexion = SingletonJDBC.getInstance().getConnection();
            PreparedStatement requete = connexion.prepareStatement(
                    "UPDATE abeille SET connecte = ? WHERE pseudo = ?"
            );
            requete.setBoolean(1, var);
            requete.setString(2, this.pseudo);
            requete.executeUpdate();
            requete.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public int getPv() { return pv; }
    public int getPollen() { return pollen; }

    public void increasePollen() {
        pollen = (pollen + 1) % 4;
        try {
            Connection connexion = SingletonJDBC.getInstance().getConnection();
            PreparedStatement requete = connexion.prepareStatement(
                    "UPDATE abeille SET qnt_pollen = ? WHERE pseudo = ?"
            );
            requete.setInt(1, pollen);
            requete.setString(2, pseudo);
            requete.executeUpdate();
            requete.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void takeHit() {
        pv -= 1;

        try {
            Connection connexion = SingletonJDBC.getInstance().getConnection();
            PreparedStatement requete = connexion.prepareStatement(
                    "UPDATE abeille SET pv = ? WHERE pseudo = ?"
            );
            requete.setInt(1, pv);
            requete.setString(2, pseudo);
            requete.executeUpdate();
            requete.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (pv > 0) damageDelay = delay;

        try {
            Connection connexion = SingletonJDBC.getInstance().getConnection();
            PreparedStatement requete = connexion.prepareStatement(
                    "UPDATE abeille SET etat = ? WHERE pseudo = ?"
            );
            requete.setInt(1, etat);
            requete.setString(2, pseudo);
            requete.executeUpdate();
            requete.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void heal() {
        if (pv < 5) {
            pv += 1;
            try {
                Connection connexion = SingletonJDBC.getInstance().getConnection();
                PreparedStatement requete = connexion.prepareStatement(
                        "UPDATE abeille SET pv = ? WHERE pseudo = ?"
                );
                requete.setInt(1, pv);
                requete.setString(2, pseudo);
                requete.executeUpdate();
                requete.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
