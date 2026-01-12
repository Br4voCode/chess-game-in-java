package chess.utils;

import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache global para imágenes de piezas de ajedrez.
 * Se carga una sola vez en el inicio de la aplicación.
 */
public class ImageCache {
    private static final Map<String, Image> cache = new HashMap<>();
    private static boolean isInitialized = false;

    /**
     * Inicializa el cache de imágenes.
     * Debe llamarse una sola vez al inicio de la aplicación.
     */
    public static void initialize() {
        System.out.println("Inicializando cache de imágenes...");
        if (isInitialized) {
            return;
        }

        String[] pieces = {
                "white_pawn", "white_rook", "white_knight", "white_bishop", "white_queen", "white_king",
                "black_pawn", "black_rook", "black_knight", "black_bishop", "black_queen", "black_king"
        };

        for (String piece : pieces) {
            try {
                String path = "/images/" + piece + ".png";
                Image img = new Image(ImageCache.class.getResourceAsStream(path));
                cache.put(piece, img);
                System.out.println("✓ Cargada imagen: " + piece);
            } catch (Exception e) {
                System.err.println("✗ Error al cargar imagen: " + piece);
                e.printStackTrace();
            }
        }

        isInitialized = true;
        System.out.println("ImageCache inicializado con " + cache.size() + " imágenes");
    }

    /**
     * Obtiene una imagen del cache.
     * 
     * @param pieceName nombre de la pieza (ej: "white_pawn")
     * @return Image o null si no existe
     */
    public static Image getImage(String pieceName) {
        return cache.get(pieceName);
    }

    /**
     * Verifica si el cache está inicializado
     */
    public static boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Obtiene el tamaño del cache
     */
    public static int getCacheSize() {
        return cache.size();
    }
}
