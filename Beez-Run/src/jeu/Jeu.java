package jeu;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class Jeu {
    private BufferedImage decor;
    private Monster uneMonster;
    private Avatar uneAvatar;
    private Abeille uneAbeille;
    private HUD hud;

    public Jeu(String name) throws IOException {
        try {
            this.decor = ImageIO.read(
                    getClass().getResource("../resources/background_jeu.png")
            );
        } catch (IOException ex) {
            Logger.getLogger(Jeu.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.uneMonster = new Monster();
        this.uneAvatar = new Avatar(name);
        this.uneAbeille = new Abeille();
        this.hud = new HUD(this.uneAvatar);
    }

    public void rendu(Graphics2D contexte) {
        contexte.drawImage(this.decor, 0, 0, null);
        this.uneMonster.rendu(contexte);
        this.uneAbeille.rendu(contexte);
        hud.rendu(contexte);
    }

    public void miseAJour() {
        this.uneMonster.miseAJour();
        this.uneAvatar.miseAJour();
        this.uneAbeille.miseAJour();
        // 1. Mise à jour de l’avatar en fonction des commandes des joueurs
        // 2. Mise à jour des autres éléments (objets, monstres, etc.)
        // 3. Gérer les interactions (collisions et autres règles)
    }

    public Avatar getAvatar(){
        return uneAvatar;
    }
}
