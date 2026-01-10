package jeu;

import java.awt.Graphics2D;
import java.io.IOException;

public class Jeu {
    private final Carte carte;
    private final Monster uneMonster;
    private final Avatar uneAvatar;
    private final Abeille uneAbeille;
    private final HUD hud;

    public Jeu(String name) throws IOException {
        this.carte = new Carte();
        this.uneMonster = new Monster();
        this.uneAvatar = new Avatar(name, this.carte);   // <- mudou aqui
        this.uneAbeille = new Abeille();
        this.hud = new HUD(this.uneAvatar);
    }

    public void rendu(Graphics2D contexte) {
        this.carte.rendu(contexte);
        this.uneMonster.rendu(contexte);
        this.uneAbeille.rendu(contexte);
        this.hud.rendu(contexte);
    }

    public void miseAJour() {
        this.uneMonster.miseAJour();
        this.uneAvatar.miseAJour();
        this.uneAbeille.miseAJour();
    }

    public Avatar getAvatar() { return uneAvatar; }

    public Carte getCarte() { return carte; }
}
