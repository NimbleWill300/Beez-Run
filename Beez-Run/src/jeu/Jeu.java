package jeu;

import java.awt.Graphics2D;
import java.io.IOException;

public class Jeu {
    private final Carte carte;
    private final Monster uneMonster;
    private final Avatar uneAvatar;
    private final Abeille uneAbeille;
    private final Ruche uneRuche;
    private final HUD hud;
    private final Fleur uneFleur;


    public Jeu(String name) throws IOException {
        this.carte = new Carte();
        this.uneMonster = new Monster();
        this.uneAvatar = new Avatar(name, this.carte);
        this.uneFleur = new Fleur(); 
        this.uneAbeille = new Abeille();
        this.uneRuche = new Ruche();
        this.hud = new HUD(this.uneAvatar);
    }

    public void rendu(Graphics2D contexte) {
        this.carte.rendu(contexte);
        this.uneMonster.rendu(contexte);
        this.uneRuche.rendu(contexte);
        this.uneFleur.rendu(contexte);
        this.uneAbeille.rendu(contexte); 
        this.hud.rendu(contexte);
    }

    public void miseAJour() {
        this.uneRuche.miseAJour(); 
        this.uneMonster.miseAJour();
        this.uneAvatar.miseAJour();
        this.uneAbeille.miseAJour();
        this.uneFleur.miseAJour();

    }

    public Avatar getAvatar() { return uneAvatar; }

    public Carte getCarte() { return carte; }
}
