package jeu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import outils.SingletonJDBC;

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

    // -------- Référence carte --------
    private final Carte carte;

    // -------- Hitbox (ajuste si besoin) --------
    // Sprite tile = 64x64, on prend une hitbox plus petite pour éviter "accrocher" partout
    private static final int HIT_W = 32;
    private static final int HIT_H = 32;
    private static final int HIT_OX = 16;  // offset à partir de x
    private static final int HIT_OY = 30;  // offset à partir de y

    public Avatar(String name, Carte carte) {
        this.carte = carte;
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
                // option: pousser doucement pour éviter "coller"
                x = pushOutX(x, nx, y);
            }

            // Y
            if (!collidesAt(x, ny)) {
                y = ny;
            } else {
                y = pushOutY(y, ny, x);
            }

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

    // Si tu bloques en X, on tente de sortir un peu (optionnel)
    private double pushOutX(double oldX, double targetX, double yFixed) {
        // on avance/recul 1px à la fois vers targetX jusqu'à collision, puis on revient
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

    // Empêche de spawn dans un mur (simple)
    private void resolveIfSpawnInWall() {
        if (!collidesAt(x, y)) return;

        // essaie de trouver un spot proche (petite recherche)
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
        // sinon, laisse tel quel (debug)
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
