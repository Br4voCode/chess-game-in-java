package chess.view;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Utilidad para cargar imágenes de piezas de ajedrez desde recursos
 */
public class PieceImageLoader {
    private static final String IMAGE_PATH = "/images/pieces/classic/";
    private static final double DEFAULT_SIZE = 24;

    /**
     * Obtiene el nombre del archivo de imagen para una pieza específica
     */
    public static String getImageFileName(String pieceSymbol) {
        return switch (pieceSymbol) {
            // Piezas blancas
            case "♔" -> "white_king.png";
            case "♕" -> "white_queen.png";
            case "♖" -> "white_rook.png";
            case "♗" -> "white_bishop.png";
            case "♘" -> "white_knight.png";
            case "♙" -> "white_pawn.png";
            
            // Piezas negras
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
     * Carga una imagen de una pieza y devuelve un ImageView con el tamaño especificado
     */
    public static ImageView loadPieceImage(String pieceSymbol, double size) {
        String fileName = getImageFileName(pieceSymbol);
        if (fileName == null) {
            return null;
        }

        try {
            Image image = new Image(PieceImageLoader.class.getResourceAsStream(IMAGE_PATH + fileName));
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(size);
            imageView.setFitHeight(size);
            imageView.setPreserveRatio(true);
            return imageView;
        } catch (Exception e) {
            System.err.println("Error loading piece image: " + fileName);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Carga una imagen de una pieza con tamaño por defecto
     */
    public static ImageView loadPieceImage(String pieceSymbol) {
        return loadPieceImage(pieceSymbol, DEFAULT_SIZE);
    }
}
