package chess.view;

import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Utilidad para cargar imágenes de piezas de ajedrez desde recursos
 */
public class PieceImageLoader {
    private static final String IMAGE_PATH = "/images/pieces/classic/";
    private static final double DEFAULT_SIZE = 24;

    /**
     * Desactiva logs ruidosos por defecto; si necesitás debug visual, ponelo en
     * true.
     */
    private static final boolean DEBUG = false;

    /** Cache global de imágenes para evitar decodificar PNG en cada repaint. */
    private static final ConcurrentMap<String, Image> IMAGE_CACHE = new ConcurrentHashMap<>();

    /**
     * Obtiene el nombre del archivo de imagen para una pieza específica
     */
    public static String getImageFileName(String pieceSymbol) {
        return switch (pieceSymbol) {

            case "♔" -> "white_king.png";
            case "♕" -> "white_queen.png";
            case "♖" -> "white_rook.png";
            case "♗" -> "white_bishop.png";
            case "♘" -> "white_knight.png";
            case "♙" -> "white_pawn.png";

            case "♚" -> "black_king.png";
            case "♛" -> "black_queen.png";
            case "♜" -> "black_rook.png";
            case "♝" -> "black_bishop.png";
            case "♞" -> "black_knight.png";
            case "♟" -> "black_pawn.png";

            default -> null;
        };
    }

    /**
     * Devuelve la Image cacheada para la pieza o null si no existe.
     *
     * Nota: devolvemos Image (no ImageView) para poder reusarla entre casillas.
     */
    public static Image loadPieceImageRaw(String pieceSymbol) {
        String fileName = getImageFileName(pieceSymbol);
        if (fileName == null) {
            return null;
        }

        final String resourcePath = IMAGE_PATH + fileName;
        return IMAGE_CACHE.computeIfAbsent(resourcePath, p -> {
            try (InputStream in = PieceImageLoader.class.getResourceAsStream(p)) {
                if (in == null) {
                    if (DEBUG) {
                        System.err.println("Piece image not found: " + p);
                    }
                    return null;
                }

                return new Image(in, 0.0, 0.0, true, true);
            } catch (Exception e) {
                if (DEBUG) {
                    System.err.println("Error loading piece image: " + p + " - " + e.getMessage());
                }
                return null;
            }
        });
    }

    /**
     * Carga una imagen de una pieza y devuelve un ImageView con el tamaño
     * especificado
     */
    public static ImageView loadPieceImage(String pieceSymbol, double size) {
        Image image = loadPieceImageRaw(pieceSymbol);
        if (image == null) {
            return null;
        }

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    /**
     * Carga una imagen de una pieza con tamaño por defecto
     */
    public static ImageView loadPieceImage(String pieceSymbol) {
        return loadPieceImage(pieceSymbol, DEFAULT_SIZE);
    }

    /**
     * Precarga todas las imágenes de piezas en el cache.
     *
     * Útil para que al entrar al juego la UI no tenga "stutter" por
     * I/O/decodificación.
     * Se puede invocar al inicio de la app (por ejemplo en el start()).
     */
    public static void preloadAllPieceImages() {

        loadPieceImageRaw("♔");
        loadPieceImageRaw("♕");
        loadPieceImageRaw("♖");
        loadPieceImageRaw("♗");
        loadPieceImageRaw("♘");
        loadPieceImageRaw("♙");

        loadPieceImageRaw("♚");
        loadPieceImageRaw("♛");
        loadPieceImageRaw("♜");
        loadPieceImageRaw("♝");
        loadPieceImageRaw("♞");
        loadPieceImageRaw("♟");
    }
}
