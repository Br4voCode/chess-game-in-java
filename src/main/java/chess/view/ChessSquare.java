package chess.view;

import chess.model.Position;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

/**
 * Componente que representa una casilla individual del tablero
 */
public class ChessSquare {
    private StackPane root;
    private Rectangle background;
    private Circle possibleMoveIndicator;
    private Circle captureIndicator;
    private Label pieceLabel;
    private Position position;
    
    // Colores
    private final Color LIGHT_COLOR = Color.rgb(240, 217, 181);
    private final Color DARK_COLOR = Color.rgb(181, 136, 99);
    private final Color SELECTED_COLOR = Color.rgb(144, 238, 144, 0.7);
    private final Color POSSIBLE_MOVE_DOT_COLOR = Color.rgb(100, 100, 100, 0.6);
    private final Color CAPTURE_INDICATOR_COLOR = Color.rgb(255, 100, 100, 0.8);

    public ChessSquare(Position position) {
        this.position = position;
        initializeSquare();
    }

    private void initializeSquare() {
        root = new StackPane();
        root.setPrefSize(60, 60);
        
        // Fondo de la casilla
        background = new Rectangle(60, 60);
        updateBaseColor();
        
        // Indicador de movimiento posible (casilla vacía)
        possibleMoveIndicator = new Circle(8);
        possibleMoveIndicator.setFill(POSSIBLE_MOVE_DOT_COLOR);
        possibleMoveIndicator.setVisible(false);
        
        // Indicador de captura (círculo hueco alrededor de la pieza)
        captureIndicator = new Circle(25);
        captureIndicator.setFill(Color.TRANSPARENT);
        captureIndicator.setStroke(CAPTURE_INDICATOR_COLOR);
        captureIndicator.setStrokeWidth(3);
        captureIndicator.setStrokeType(StrokeType.OUTSIDE);
        captureIndicator.setVisible(false);
        
        // Label para la pieza
        pieceLabel = new Label();
        pieceLabel.setStyle("-fx-font-size: 28; -fx-font-weight: bold;");
        
        // Orden: fondo, indicador movimiento, pieza, indicador captura
        root.getChildren().addAll(background, possibleMoveIndicator, pieceLabel, captureIndicator);
    }

    private void updateBaseColor() {
        Color baseColor = (position.getRow() + position.getCol()) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR;
        background.setFill(baseColor);
    }

    public void updateAppearance(String pieceSymbol, boolean isSelected, boolean isPossibleMove, boolean isCaptureMove) {
        // Actualizar símbolo de la pieza
        if (pieceSymbol == null || pieceSymbol.isEmpty()) {
            pieceLabel.setText("");
        } else {
            pieceLabel.setText(pieceSymbol);
        }
        
        // Lógica de visualización
        if (isSelected) {
            // Cuando está seleccionada: fondo verde, sin indicadores
            background.setFill(SELECTED_COLOR);
            possibleMoveIndicator.setVisible(false);
            captureIndicator.setVisible(false);
        } else if (isPossibleMove) {
            if (isCaptureMove) {
                // Movimiento de captura: círculo rojo alrededor de la pieza
                captureIndicator.setVisible(true);
                possibleMoveIndicator.setVisible(false);
                updateBaseColor();
            } else {
                // Movimiento normal: círculo gris en el centro
                captureIndicator.setVisible(false);
                possibleMoveIndicator.setVisible(true);
                updateBaseColor();
            }
        } else {
            // Estado normal
            possibleMoveIndicator.setVisible(false);
            captureIndicator.setVisible(false);
            updateBaseColor();
        }
    }

    public void clearHighlight() {
        possibleMoveIndicator.setVisible(false);
        captureIndicator.setVisible(false);
        updateBaseColor();
    }

    public void setOnMouseClicked(javafx.event.EventHandler<javafx.scene.input.MouseEvent> handler) {
        root.setOnMouseClicked(handler);
    }

    public StackPane getRoot() {
        return root;
    }

    public Position getPosition() {
        return position;
    }
}