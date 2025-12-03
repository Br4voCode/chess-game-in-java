package chess.view;

import chess.model.Position;
import javafx.scene.control.Label;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * Componente que representa una casilla individual del tablero
 */
public class ChessSquare {
    private StackPane root;
    private Rectangle background;
    private Rectangle selectionOverlay; // Capa semitransparente para el efecto
    private Circle possibleMoveIndicator;
    private Circle captureIndicator;
    private Label pieceLabel;
    private ImageView pieceImageView;
    private Position position;
    
    // Colores originales
    private final Color LIGHT_COLOR = Color.rgb(240, 217, 181);
    private final Color DARK_COLOR = Color.rgb(181, 136, 99);
    private final Color SELECTION_GLOW_COLOR = Color.rgb(144, 238, 144, 0.5);
    private final Color SELECTION_TINT = Color.rgb(144, 238, 144, 0.3);
    private final Color POSSIBLE_MOVE_DOT_COLOR = Color.rgb(100, 100, 100, 0.6);
    private final Color CAPTURE_INDICATOR_COLOR = Color.rgb(255, 100, 100, 0.8);
    
    // Efectos
    private InnerShadow innerGlow;
    private ScaleTransition piecePulseAnimation;
    
    // Estado
    private boolean isSelected = false;
    private boolean useImages = false;
    private String pieceSet = "default";
    private String currentPieceSymbol = "";

    public ChessSquare(Position position) {
        this.position = position;
        initializeSquare();
        setupEffectsAndAnimations();
    }

    private void initializeSquare() {
        root = new StackPane();
        root.setPrefSize(60, 60);
        
        // Fondo de la casilla
        background = new Rectangle(60, 60);
        background.setArcWidth(6);
        background.setArcHeight(6);
        
        // Capa de selección (inicialmente invisible)
        selectionOverlay = new Rectangle(60, 60);
        selectionOverlay.setArcWidth(6);
        selectionOverlay.setArcHeight(6);
        selectionOverlay.setFill(Color.TRANSPARENT);
        selectionOverlay.setVisible(false);
        
        // Indicador de movimiento posible
        possibleMoveIndicator = new Circle(7);
        possibleMoveIndicator.setFill(POSSIBLE_MOVE_DOT_COLOR);
        possibleMoveIndicator.setVisible(false);
        possibleMoveIndicator.setOpacity(0);
        
        // Indicador de captura
        captureIndicator = new Circle(25);
        captureIndicator.setFill(Color.TRANSPARENT);
        captureIndicator.setStroke(CAPTURE_INDICATOR_COLOR);
        captureIndicator.setStrokeWidth(3);
        captureIndicator.setStrokeType(StrokeType.OUTSIDE);
        captureIndicator.setVisible(false);
        captureIndicator.setScaleX(0);
        captureIndicator.setScaleY(0);
        
        // Label para texto
        pieceLabel = new Label();
        pieceLabel.setStyle("-fx-font-size: 28; -fx-font-weight: bold;");
        
        // ImageView para piezas con imágenes
        pieceImageView = new ImageView();
        pieceImageView.setFitWidth(50);
        pieceImageView.setFitHeight(50);
        pieceImageView.setPreserveRatio(true);
        pieceImageView.setVisible(false);
        
        // Orden de renderizado: fondo, overlay, indicadores, pieza
        root.getChildren().addAll(
            background, 
            selectionOverlay,
            possibleMoveIndicator, 
            captureIndicator,
            pieceLabel, 
            pieceImageView
        );
        
        // Ahora que todos los componentes están inicializados, actualizar el color base
        updateBaseColor();
    }

    private void setupEffectsAndAnimations() {
        // InnerShadow para el efecto de glow interior
        innerGlow = new InnerShadow();
        innerGlow.setColor(SELECTION_GLOW_COLOR);
        innerGlow.setRadius(25);
        innerGlow.setChoke(0.5);
        
        // Animación de pulso para la pieza
        piecePulseAnimation = new ScaleTransition(Duration.millis(800));
        piecePulseAnimation.setCycleCount(ScaleTransition.INDEFINITE);
        piecePulseAnimation.setAutoReverse(true);
    }

    private void updateBaseColor() {
        Color baseColor = (position.getRow() + position.getCol()) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR;
        background.setFill(baseColor);
        background.setEffect(null); // Remover cualquier efecto
        
        // Ocultar overlay de selección si existe
        if (selectionOverlay != null) {
            selectionOverlay.setVisible(false);
            selectionOverlay.setFill(Color.TRANSPARENT);
        }
    }

    public void updateAppearance(String pieceSymbol, boolean isSelected, 
                                boolean isPossibleMove, boolean isCaptureMove) {
        // Guardar el símbolo de la pieza
        if (pieceSymbol != null) {
            currentPieceSymbol = pieceSymbol;
        }
        
        // Actualizar representación de la pieza
        updatePieceRepresentation(pieceSymbol);
        
        // Manejar selección
        handleSelection(isSelected);
        
        // Manejar movimientos posibles
        handlePossibleMoves(isPossibleMove, isCaptureMove);
        
        // Si no está seleccionada y no es movimiento posible, restaurar estado normal
        if (!isSelected && !isPossibleMove) {
            updateBaseColor();
        }
    }

    private void updatePieceRepresentation(String pieceSymbol) {
        if (useImages) {
            // Usar imágenes
            if (pieceSymbol == null || pieceSymbol.isEmpty()) {
                pieceLabel.setVisible(false);
                pieceImageView.setVisible(false);
            } else {
                pieceLabel.setVisible(false);
                pieceImageView.setVisible(true);
                loadPieceImage(pieceSymbol);
            }
        } else {
            // Usar texto
            if (pieceSymbol == null || pieceSymbol.isEmpty()) {
                pieceLabel.setText("");
                pieceImageView.setVisible(false);
            } else {
                pieceLabel.setText(pieceSymbol);
                pieceLabel.setVisible(true);
                pieceImageView.setVisible(false);
                
                // Color del texto basado en el color de la pieza
                char firstChar = pieceSymbol.charAt(0);
                boolean isWhitePiece = firstChar == '♔' || firstChar == '♕' || firstChar == '♖' || 
                                     firstChar == '♗' || firstChar == '♘' || firstChar == '♙';
                pieceLabel.setTextFill(isWhitePiece ? Color.WHITE : Color.rgb(40, 40, 40));
            }
        }
    }

    private void loadPieceImage(String pieceSymbol) {
        try {
            String imagePath = String.format("/pieces/%s/%s.png", pieceSet, getImageName(pieceSymbol));
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            pieceImageView.setImage(image);
        } catch (Exception e) {
            // Fallback a texto
            System.err.println("Error loading image for piece: " + pieceSymbol + ", " + e.getMessage());
            pieceImageView.setVisible(false);
            if (pieceSymbol != null && !pieceSymbol.isEmpty()) {
                pieceLabel.setText(pieceSymbol);
                pieceLabel.setVisible(true);
            }
        }
    }

    private String getImageName(String pieceSymbol) {
        switch (pieceSymbol) {
            case "♔": return "white_king";
            case "♕": return "white_queen";
            case "♖": return "white_rook";
            case "♗": return "white_bishop";
            case "♘": return "white_knight";
            case "♙": return "white_pawn";
            case "♚": return "black_king";
            case "♛": return "black_queen";
            case "♜": return "black_rook";
            case "♝": return "black_bishop";
            case "♞": return "black_knight";
            case "♟": return "black_pawn";
            default: return pieceSymbol.toLowerCase();
        }
    }

    private void handleSelection(boolean shouldBeSelected) {
        if (this.isSelected != shouldBeSelected) {
            if (shouldBeSelected) {
                selectSquare();
            } else {
                deselectSquare();
            }
            this.isSelected = shouldBeSelected;
        }
    }

    private void selectSquare() {
        // 1. Aplicar efecto de glow interior con animación
        if (innerGlow != null) {
            background.setEffect(innerGlow);
        }
        
        // 2. Añadir capa semitransparente verde con animación
        if (selectionOverlay != null) {
            selectionOverlay.setFill(SELECTION_TINT);
            selectionOverlay.setVisible(true);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), selectionOverlay);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
        
        // 3. Iniciar pulso en la pieza
        startPiecePulse();
        
        // 4. Oscurecer ligeramente el fondo (10%)
        Color baseColor = (position.getRow() + position.getCol()) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR;
        Color selectedColor = Color.rgb(
            (int)(baseColor.getRed() * 255 * 0.9),
            (int)(baseColor.getGreen() * 255 * 0.9),
            (int)(baseColor.getBlue() * 255 * 0.9)
        );
        background.setFill(selectedColor);
    }

    private void deselectSquare() {
        // 1. Quitar efecto de glow
        background.setEffect(null);
        
        // 2. Ocultar capa de selección con animación
        if (selectionOverlay != null && selectionOverlay.isVisible()) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), selectionOverlay);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                selectionOverlay.setVisible(false);
                selectionOverlay.setFill(Color.TRANSPARENT);
            });
            fadeOut.play();
        }
        
        // 3. Detener pulso
        stopPiecePulse();
        
        // 4. Restaurar color base después de un breve delay
        PauseTransition pause = new PauseTransition(Duration.millis(200));
        pause.setOnFinished(e -> updateBaseColor());
        pause.play();
    }

    private void startPiecePulse() {
        // Determinar qué nodo animar (imagen o label)
        javafx.scene.Node targetNode = null;
        if (useImages && pieceImageView.isVisible()) {
            targetNode = pieceImageView;
        } else if (pieceLabel.isVisible()) {
            targetNode = pieceLabel;
        }
        
        if (targetNode != null && piecePulseAnimation != null) {
            // Restaurar escala antes de empezar
            targetNode.setScaleX(1.0);
            targetNode.setScaleY(1.0);
            
            // Configurar animación
            piecePulseAnimation.setNode(targetNode);
            piecePulseAnimation.setFromX(0.95);
            piecePulseAnimation.setFromY(0.95);
            piecePulseAnimation.setToX(1.05);
            piecePulseAnimation.setToY(1.05);
            
            // Pequeña pausa antes de empezar
            PauseTransition pause = new PauseTransition(Duration.millis(100));
            pause.setOnFinished(e -> piecePulseAnimation.play());
            pause.play();
        }
    }

    private void stopPiecePulse() {
        if (piecePulseAnimation != null) {
            piecePulseAnimation.stop();
        }
        
        // Restaurar escala normal
        if (pieceImageView.isVisible()) {
            pieceImageView.setScaleX(1.0);
            pieceImageView.setScaleY(1.0);
        }
        if (pieceLabel.isVisible()) {
            pieceLabel.setScaleX(1.0);
            pieceLabel.setScaleY(1.0);
        }
    }

    private void handlePossibleMoves(boolean isPossibleMove, boolean isCaptureMove) {
        if (isPossibleMove) {
            if (isCaptureMove) {
                showCaptureIndicator();
            } else {
                showPossibleMoveIndicator();
            }
        } else {
            hideIndicators();
        }
    }

    private void showPossibleMoveIndicator() {
        possibleMoveIndicator.setVisible(true);
        captureIndicator.setVisible(false);
        
        // Animación de fade in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), possibleMoveIndicator);
        fadeIn.setFromValue(possibleMoveIndicator.getOpacity());
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void showCaptureIndicator() {
        captureIndicator.setVisible(true);
        possibleMoveIndicator.setVisible(false);
        
        // Animación de escala
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(250), captureIndicator);
        scaleIn.setFromX(captureIndicator.getScaleX());
        scaleIn.setFromY(captureIndicator.getScaleY());
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        scaleIn.play();
    }

    private void hideIndicators() {
        if (possibleMoveIndicator.isVisible()) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), possibleMoveIndicator);
            fadeOut.setFromValue(possibleMoveIndicator.getOpacity());
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                possibleMoveIndicator.setVisible(false);
                possibleMoveIndicator.setOpacity(0);
            });
            fadeOut.play();
        }
        
        if (captureIndicator.isVisible()) {
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), captureIndicator);
            scaleOut.setFromX(captureIndicator.getScaleX());
            scaleOut.setFromY(captureIndicator.getScaleY());
            scaleOut.setToX(0.0);
            scaleOut.setToY(0.0);
            scaleOut.setOnFinished(e -> {
                captureIndicator.setVisible(false);
                captureIndicator.setScaleX(0);
                captureIndicator.setScaleY(0);
            });
            scaleOut.play();
        }
    }

    public void clearHighlight() {
        if (isSelected) {
            deselectSquare();
            isSelected = false;
        }
        hideIndicators();
        updateBaseColor();
    }

    // Métodos de configuración
    public void setUseImages(boolean useImages) {
        this.useImages = useImages;
        // Actualizar la representación con el nuevo modo
        if (!currentPieceSymbol.isEmpty()) {
            updatePieceRepresentation(currentPieceSymbol);
        }
    }
    
    public void setPieceSet(String pieceSet) {
        this.pieceSet = pieceSet;
        // Recargar imagen si estamos usando imágenes
        if (useImages && !currentPieceSymbol.isEmpty()) {
            loadPieceImage(currentPieceSymbol);
        }
    }
    
    // Método para animar movimiento de pieza
    public void animatePieceMoveFrom(ChessSquare fromSquare, Runnable onFinished) {
        if (useImages && pieceImageView.getImage() != null) {
            // Crear copia flotante de la pieza
            ImageView floatingPiece = new ImageView(pieceImageView.getImage());
            floatingPiece.setFitWidth(50);
            floatingPiece.setFitHeight(50);
            floatingPiece.setPreserveRatio(true);
            
            // Obtener posición relativa
            double startX = fromSquare.getRoot().getLayoutX() - root.getLayoutX();
            double startY = fromSquare.getRoot().getLayoutY() - root.getLayoutY();
            
            floatingPiece.setTranslateX(startX);
            floatingPiece.setTranslateY(startY);
            floatingPiece.setOpacity(0.9);
            
            root.getChildren().add(floatingPiece);
            
            // Animación de movimiento
            javafx.animation.TranslateTransition move = 
                new javafx.animation.TranslateTransition(Duration.millis(400), floatingPiece);
            move.setToX(0);
            move.setToY(0);
            
            // Animación de opacidad
            FadeTransition fade = new FadeTransition(Duration.millis(400), floatingPiece);
            fade.setFromValue(0.9);
            fade.setToValue(1.0);
            
            // Ejecutar en paralelo
            javafx.animation.ParallelTransition parallel = 
                new javafx.animation.ParallelTransition(move, fade);
            
            parallel.setOnFinished(e -> {
                root.getChildren().remove(floatingPiece);
                if (onFinished != null) {
                    onFinished.run();
                }
            });
            
            parallel.play();
        } else if (onFinished != null) {
            // Si no hay animación, ejecutar callback inmediatamente
            onFinished.run();
        }
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
    
    public String getPieceSymbol() {
        return pieceLabel.getText();
    }
    
    public boolean isSelected() {
        return isSelected;
    }
    
    public boolean isEmpty() {
        return (pieceLabel.getText() == null || pieceLabel.getText().isEmpty()) && 
               (!pieceImageView.isVisible() || pieceImageView.getImage() == null);
    }
}