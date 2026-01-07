package chess.view;

import chess.model.Position;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Label;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.util.Duration;

/**
 * Componente que representa una casilla individual del tablero
 */
public class ChessSquare {
	private StackPane root;
	private Rectangle background;
	private Rectangle selectionOverlay;
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
	private final Color MOVE_HIGHLIGHT_COLOR = Color.rgb(255, 255, 0, 0.4);

	// Efectos
	private InnerShadow innerGlow;
	private ScaleTransition piecePulseAnimation;

	// Estado
	private boolean isSelected = false;
	private boolean useImages = true; // Siempre usar imágenes
	private String pieceSet = "classic";
	private String currentPieceSymbol = "";

	public ChessSquare(Position position) {
		this.position = position;
		initializeSquare();
		setupEffectsAndAnimations();
	}

	private void initializeSquare() {
		root = new StackPane();
		root.setMinSize(1, 1);
		root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

		// Fondo de la casilla
		background = new Rectangle();
		background.widthProperty().bind(root.widthProperty());
		background.heightProperty().bind(root.heightProperty());
		background.arcWidthProperty().bind(
				Bindings.createDoubleBinding(
						() -> Math.min(12, Math.max(4, Math.min(root.getWidth(), root.getHeight()) * 0.12)),
						root.widthProperty(),
						root.heightProperty()));
		background.arcHeightProperty().bind(background.arcWidthProperty());

		// Capa de selección (inicialmente invisible)
		selectionOverlay = new Rectangle();
		selectionOverlay.widthProperty().bind(root.widthProperty());
		selectionOverlay.heightProperty().bind(root.heightProperty());
		selectionOverlay.arcWidthProperty().bind(background.arcWidthProperty());
		selectionOverlay.arcHeightProperty().bind(background.arcHeightProperty());
		selectionOverlay.setFill(Color.TRANSPARENT);
		selectionOverlay.setVisible(false);

		// Indicador de movimiento posible
		possibleMoveIndicator = new Circle();
		possibleMoveIndicator.radiusProperty().bind(
				Bindings.createDoubleBinding(
						() -> Math.max(3, Math.min(root.getWidth(), root.getHeight()) * 0.12),
						root.widthProperty(),
						root.heightProperty()));
		possibleMoveIndicator.setFill(POSSIBLE_MOVE_DOT_COLOR);
		possibleMoveIndicator.setVisible(false);
		possibleMoveIndicator.setOpacity(0);

		// Indicador de captura
		captureIndicator = new Circle();
		captureIndicator.radiusProperty().bind(
				Bindings.createDoubleBinding(
						() -> Math.max(10, Math.min(root.getWidth(), root.getHeight()) * 0.42),
						root.widthProperty(),
						root.heightProperty()));
		captureIndicator.setFill(Color.TRANSPARENT);
		captureIndicator.setStroke(CAPTURE_INDICATOR_COLOR);
		captureIndicator.strokeWidthProperty().bind(
				Bindings.createDoubleBinding(
						() -> Math.min(4, Math.max(2, Math.min(root.getWidth(), root.getHeight()) * 0.06)),
						root.widthProperty(),
						root.heightProperty()));
		captureIndicator.setStrokeType(StrokeType.OUTSIDE);
		captureIndicator.setVisible(false);
		captureIndicator.setScaleX(0);
		captureIndicator.setScaleY(0);

		// Label para texto
		pieceLabel = new Label();
		pieceLabel.setStyle("-fx-font-size: 28; -fx-font-weight: bold;");
		root.widthProperty().addListener((obs, oldV, newV) -> updatePieceSizing());
		root.heightProperty().addListener((obs, oldV, newV) -> updatePieceSizing());

		// ImageView para piezas con imágenes
		pieceImageView = new ImageView();
		pieceImageView.fitWidthProperty().bind(
				Bindings.createDoubleBinding(
						() -> Math.max(16, Math.min(root.getWidth(), root.getHeight()) * 0.82),
						root.widthProperty(),
						root.heightProperty()));
		pieceImageView.fitHeightProperty().bind(pieceImageView.fitWidthProperty());
		pieceImageView.setPreserveRatio(true);
		pieceImageView.setVisible(false);

		// Orden de renderizado: fondo, overlay, indicadores, pieza
		root.getChildren().addAll(
				background,
				selectionOverlay,
				possibleMoveIndicator,
				captureIndicator,
				pieceLabel,
				pieceImageView);

		updateBaseColor();
		updatePieceSizing();
	}

	private void updatePieceSizing() {
		double size = Math.min(root.getWidth(), root.getHeight());
		if (size <= 0) {
			return;
		}
		int fontSize = (int) Math.max(12, Math.min(48, size * 0.55));
		pieceLabel.setStyle("-fx-font-size: " + fontSize + "; -fx-font-weight: bold;");
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
		javafx.application.Platform.runLater(() -> {
			Color baseColor = (position.getRow() + position.getCol()) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR;
			background.setFill(baseColor);
			background.setEffect(null);

			if (selectionOverlay != null) {
				selectionOverlay.setVisible(false);
				selectionOverlay.setFill(Color.TRANSPARENT);
			}
		});
	}

	public void updateAppearance(String pieceSymbol, boolean isSelected,
			boolean isPossibleMove, boolean isCaptureMove) {

		// Guardar el símbolo de la pieza
		if (pieceSymbol != null) {
			currentPieceSymbol = pieceSymbol;
		}

		// Actualizar pieza PRIMERO
		updatePieceRepresentation(pieceSymbol);

		// Manejar selección y movimientos
		handleSelection(isSelected);
		handlePossibleMoves(isPossibleMove, isCaptureMove);

		// Solo restaurar color base si NO está seleccionada y NO es movimiento posible
		// (incluyendo capturas). Si hay highlight activo, no resetear el fondo.
		if (!isSelected && !isPossibleMove && !isCaptureMove) {
			updateBaseColor();
		}
	}

	private void updatePieceRepresentation(String pieceSymbol) {
		// Siempre ocultar el label de texto
		pieceLabel.setVisible(false);

		if (pieceSymbol == null || pieceSymbol.isEmpty()) {
			pieceImageView.setVisible(false);
		} else {
			pieceImageView.setVisible(true);
			loadPieceImage(pieceSymbol);
		}
	}

	private void loadPieceImage(String pieceSymbol) {
		String imageName = getImageName(pieceSymbol);
		String imagePath = String.format("/images/pieces/%s/%s.png", pieceSet, imageName);

		// Debug: imprimir información
		System.out.println("DEBUG: Cargando pieza - Símbolo: '" + pieceSymbol + "' -> Imagen: '" + imageName
				+ "' -> Ruta: '" + imagePath + "'");

		try {
			Image image = new Image(getClass().getResourceAsStream(imagePath));
			if (image.isError()) {
				System.err.println("ERROR: Imagen con error: " + imagePath);
				pieceImageView.setVisible(false);
			} else {
				pieceImageView.setImage(image);
				System.out.println("SUCCESS: Imagen cargada correctamente: " + imagePath);
			}
		} catch (Exception e) {
			System.err.println("EXCEPTION: Error cargando imagen: " + imagePath + " - " + e.getMessage());
			pieceImageView.setVisible(false);
		}
	}

	private String getImageName(String pieceSymbol) {
		// Map Unicode chess symbols to image file names
		switch (pieceSymbol) {
			// White pieces (Unicode symbols)
			case "♔":
				return "white_king";
			case "♕":
				return "white_queen";
			case "♖":
				return "white_rook";
			case "♗":
				return "white_bishop";
			case "♘":
				return "white_knight";
			case "♙":
				return "white_pawn";

			// Black pieces (Unicode symbols)
			case "♚":
				return "black_king";
			case "♛":
				return "black_queen";
			case "♜":
				return "black_rook";
			case "♝":
				return "black_bishop";
			case "♞":
				return "black_knight";
			case "♟":
				return "black_pawn";

			default:
				System.err.println("WARN: Unknown piece symbol: '" + pieceSymbol + "'");
				return "white_pawn"; // Fallback
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
		if (innerGlow != null) {
			background.setEffect(innerGlow);
		}

		if (selectionOverlay != null) {
			selectionOverlay.setFill(SELECTION_TINT);
			selectionOverlay.setVisible(true);

			FadeTransition fadeIn = new FadeTransition(Duration.millis(300), selectionOverlay);
			fadeIn.setFromValue(0.0);
			fadeIn.setToValue(1.0);
			fadeIn.play();
		}

		startPiecePulse();

		Color baseColor = (position.getRow() + position.getCol()) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR;
		Color selectedColor = Color.rgb(
				(int) (baseColor.getRed() * 255 * 0.9),
				(int) (baseColor.getGreen() * 255 * 0.9),
				(int) (baseColor.getBlue() * 255 * 0.9));
		background.setFill(selectedColor);
	}

	private void deselectSquare() {
		// 1. Detener TODAS las animaciones inmediatamente
		stopAllAnimations();

		// 2. Cancelar transiciones pendientes
		cancelPendingTransitions();

		// 3. Limpiar efectos visuales inmediatamente
		background.setEffect(null);

		// 4. Restaurar color base SIN animación (inmediato)
		Color baseColor = (position.getRow() + position.getCol()) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR;
		background.setFill(baseColor);

		// 5. Ocultar overlay con fade out rápido
		if (selectionOverlay != null && selectionOverlay.isVisible()) {
			// Cancelar animaciones previas
			FadeTransition previousFade = (FadeTransition) selectionOverlay.getProperties().get("currentFade");
			if (previousFade != null) {
				previousFade.stop();
			}

			FadeTransition fadeOut = new FadeTransition(Duration.millis(150), selectionOverlay);
			selectionOverlay.getProperties().put("currentFade", fadeOut);

			fadeOut.setFromValue(selectionOverlay.getOpacity());
			fadeOut.setToValue(0.0);
			fadeOut.setOnFinished(e -> {
				selectionOverlay.setVisible(false);
				selectionOverlay.setFill(Color.TRANSPARENT);
				selectionOverlay.setOpacity(1.0);
				selectionOverlay.getProperties().remove("currentFade");
			});
			fadeOut.play();
		} else {
			selectionOverlay.setVisible(false);
			selectionOverlay.setFill(Color.TRANSPARENT);
		}

		// 6. Asegurar que las piezas no tengan escala residual
		if (pieceImageView != null) {
			pieceImageView.setScaleX(1.0);
			pieceImageView.setScaleY(1.0);
			pieceImageView.setOpacity(1.0);
		}

		if (pieceLabel != null) {
			pieceLabel.setScaleX(1.0);
			pieceLabel.setScaleY(1.0);
			pieceLabel.setOpacity(1.0);
		}
	}

	private void cancelPendingTransitions() {
		// Cancelar cualquier transición pendiente
		if (possibleMoveIndicator.getProperties().containsKey("currentFade")) {
			FadeTransition ft = (FadeTransition) possibleMoveIndicator.getProperties().get("currentFade");
			if (ft != null)
				ft.stop();
			possibleMoveIndicator.getProperties().remove("currentFade");
		}

		if (captureIndicator.getProperties().containsKey("currentScale")) {
			ScaleTransition st = (ScaleTransition) captureIndicator.getProperties().get("currentScale");
			if (st != null)
				st.stop();
			captureIndicator.getProperties().remove("currentScale");
		}
	}

	private void startPiecePulse() {
		javafx.scene.Node targetNode = null;
		if (useImages && pieceImageView.isVisible()) {
			targetNode = pieceImageView;
		} else if (pieceLabel.isVisible()) {
			targetNode = pieceLabel;
		}

		if (targetNode != null && piecePulseAnimation != null) {
			targetNode.setScaleX(1.0);
			targetNode.setScaleY(1.0);

			piecePulseAnimation.setNode(targetNode);
			piecePulseAnimation.setFromX(0.95);
			piecePulseAnimation.setFromY(0.95);
			piecePulseAnimation.setToX(1.05);
			piecePulseAnimation.setToY(1.05);

			PauseTransition pause = new PauseTransition(Duration.millis(100));
			pause.setOnFinished(e -> piecePulseAnimation.play());
			pause.play();
		}
	}

	private void stopPiecePulse() {
		if (piecePulseAnimation != null) {
			piecePulseAnimation.stop();
		}

		// Restaurar escala inmediatamente
		if (pieceImageView != null && pieceImageView.isVisible()) {
			pieceImageView.setScaleX(1.0);
			pieceImageView.setScaleY(1.0);
		}
		if (pieceLabel != null && pieceLabel.isVisible()) {
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

		FadeTransition fadeIn = new FadeTransition(Duration.millis(200), possibleMoveIndicator);
		fadeIn.setFromValue(possibleMoveIndicator.getOpacity());
		fadeIn.setToValue(1.0);
		fadeIn.play();
	}

	private void showCaptureIndicator() {
		captureIndicator.setVisible(true);
		possibleMoveIndicator.setVisible(false);

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

	public void highlightAsMovePart() {
		javafx.application.Platform.runLater(() -> {
			// Usar el selectionOverlay para mostrar un tinte amarillo
			selectionOverlay.setFill(MOVE_HIGHLIGHT_COLOR);
			selectionOverlay.setVisible(true);
			selectionOverlay.setOpacity(1.0);

			// Opcionalmente, agregar un ligero brillo
			InnerShadow highlightGlow = new InnerShadow();
			highlightGlow.setColor(Color.YELLOW);
			highlightGlow.setRadius(15);
			background.setEffect(highlightGlow);
		});
	}

	public void clearHighlight() {
		// 1. Detener todas las animaciones
		if (piecePulseAnimation != null) {
			piecePulseAnimation.stop();
		}

		// 2. Cancelar cualquier transición pendiente
		cancelPendingTransitions();

		// 3. Restaurar estado inmediatamente
		isSelected = false;

		// 4. Limpiar efectos visuales INMEDIATAMENTE (sin animaciones)
		background.setEffect(null);
		selectionOverlay.setVisible(false);
		selectionOverlay.setFill(Color.TRANSPARENT);
		selectionOverlay.setOpacity(1.0);

		// 5. Ocultar indicadores
		possibleMoveIndicator.setVisible(false);
		possibleMoveIndicator.setOpacity(0);
		captureIndicator.setVisible(false);
		captureIndicator.setScaleX(0);
		captureIndicator.setScaleY(0);

		// 6. Restaurar escala de piezas
		if (pieceImageView != null) {
			pieceImageView.setScaleX(1.0);
			pieceImageView.setScaleY(1.0);
			pieceImageView.setOpacity(1.0);
		}

		if (pieceLabel != null) {
			pieceLabel.setScaleX(1.0);
			pieceLabel.setScaleY(1.0);
			pieceLabel.setOpacity(1.0);
		}

		// 7. Restaurar color base
		updateBaseColor();
	}

	private void stopAllAnimations() {
		// Detener animación de pulso
		if (piecePulseAnimation != null) {
			piecePulseAnimation.stop();
		}

		// Detener cualquier transición pendiente en indicadores
		if (selectionOverlay.getProperties().containsKey("currentFade")) {
			FadeTransition ft = (FadeTransition) selectionOverlay.getProperties().get("currentFade");
			if (ft != null)
				ft.stop();
		}

		// Restaurar escala inmediatamente
		if (pieceImageView.isVisible()) {
			pieceImageView.setScaleX(1.0);
			pieceImageView.setScaleY(1.0);
		}
		if (pieceLabel.isVisible()) {
			pieceLabel.setScaleX(1.0);
			pieceLabel.setScaleY(1.0);
		}
	}

	// Getters y setters importantes para animaciones
	public javafx.scene.image.Image getPieceImage() {
		return pieceImageView.getImage();
	}

	public String getPieceSymbol() {
		return pieceLabel.getText();
	}

	public ImageView getPieceImageView() {
		return pieceImageView;
	}

	public Label getPieceLabel() {
		return pieceLabel;
	}

	public boolean isUsingImages() {
		return useImages;
	}

	public void setUseImages(boolean useImages) {
		this.useImages = useImages;
		if (!currentPieceSymbol.isEmpty()) {
			updatePieceRepresentation(currentPieceSymbol);
		}
	}

	public void setPieceSet(String pieceSet) {
		this.pieceSet = pieceSet;
		if (useImages && !currentPieceSymbol.isEmpty()) {
			loadPieceImage(currentPieceSymbol);
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

	public boolean isSelected() {
		return isSelected;
	}

	public boolean isEmpty() {
		return (pieceLabel.getText() == null || pieceLabel.getText().isEmpty()) &&
				(!pieceImageView.isVisible() || pieceImageView.getImage() == null);
	}
}