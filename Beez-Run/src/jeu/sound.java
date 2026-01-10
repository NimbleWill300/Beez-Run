package jeu;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class Sound {
    private Clip clip;

    /** Toca em loop um WAV que está no classpath (resources). */
    public void playLoopFromResource(String resourcePath) {
        stop();

        try {
            URL url = getClass().getResource(resourcePath);
            if (url == null) {
                throw new IllegalArgumentException("Ressource introuvable: " + resourcePath);
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            clip.open(ais);

            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
    }

    public void setVolume(float volume0to1) {
        if (clip == null) return;
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) return;

        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

        // converte 0..1 para dB (com clamp)
        volume0to1 = Math.max(0.0001f, Math.min(1.0f, volume0to1));
        float dB = (float) (20.0 * Math.log10(volume0to1));
        dB = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB));

        gain.setValue(dB);
    }
}
