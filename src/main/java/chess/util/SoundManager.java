package chess.util;

import java.net.URL;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Gestor para reproducir efectos de sonido
 */
public class SoundManager {
    
    /**
     * Reproduce un archivo de audio desde los recursos
     * @param soundFileName nombre del archivo de sonido (ej: "1.mp3")
     */
    public static void playSound(String soundFileName) {
        try {
            // Obtener el archivo de recursos
            URL soundURL = SoundManager.class.getResource("/" + soundFileName);
            
            if (soundURL == null) {
                System.err.println("No se encontró el archivo de sonido: " + soundFileName);
                return;
            }
            
            // Reproducir el sonido en un hilo separado para no bloquear la UI
            new Thread(() -> {
                try {
                    Media media = new Media(soundURL.toString());
                    MediaPlayer mediaPlayer = new MediaPlayer(media);
                    mediaPlayer.play();
                } catch (Exception e) {
                    System.err.println("Error al reproducir sonido: " + e.getMessage());
                }
            }).start();
        } catch (Exception e) {
            System.err.println("Error al cargar sonido: " + e.getMessage());
        }
    }
}
