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

	private final Color LIGHT_COLOR = Color.rgb(240, 217, 181);
	private final Color DARK_COLOR = Color.rgb(181, 136, 99);
	private final Color SELECTION_GLOW_COLOR = Color.rgb(144, 238, 144, 0.5);
	private final Color SELECTION_TINT = Color.rgb(144, 238, 144, 0.3);
	private final Color POSSIBLE_MOVE_DOT_COLOR = Color.rgb(100, 100, 100, 0.6);
	private final Color CAPTURE_INDICATOR_COLOR = Color.rgb(255, 100, 100, 0.8);
	private final Color MOVE_HIGHLIGHT_COLOR = Color.rgb(255, 255, 0, 0.4);
	private final Color HINT_HIGHLIGHT_COLOR = Color.rgb(0, 255, 255, 0.4);

	private InnerShadow innerGlow;
	private ScaleTransition piecePulseAnimation;

	private boolean isSelected = false;
	private boolean useImages = true;
	private String currentPieceSymbol = "";

	/** Logs visuales muy ruidosos (no activar en producción). */
	private static final boolean DEBUG_IMAGES = false;

	public ChessSquare(Position position) {
		this.position = position;
		initializeSquare();
		setupEffectsAndAnimations();
	}

	private void initializeSquare() {
		root = new StackPane();
		root.setMinSize(1, 1);
		root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

		background = new Rectangle();
		background.widthProperty().bind(root.widthProperty());
		background.heightProperty().bind(root.heightProperty());
		background.arcWidthProperty().bind(
				Bindings.createDoubleBinding(
						() -> Math.min(12, Math.max(4, Math.min(root.getWidth(), root.getHeight()) * 0.12)),
						root.widthProperty(),
						root.heightProperty()));
		background.arcHeightProperty().bind(background.arcWidthProperty());

		selectionOverlay = new Rectangle();
		selectionOverlay.widthProperty().bind(root.widthProperty());
		selectionOverlay.heightProperty().bind(root.heightProperty());
		selectionOverlay.arcWidthProperty().bind(background.arcWidthProperty());
		selectionOverlay.arcHeightProperty().bind(background.arcHeightProperty());
		selectionOverlay.setFill(Color.TRANSPARENT);
		selectionOverlay.setVisible(false);

		possibleMoveIndicator = new Circle();
		possibleMoveIndicator.radiusProperty().bind(
				Bindings.createDoubleBinding(
						() -> Math.max(3, Math.min(root.getWidth(), root.getHeight()) * 0.12),
						root.widthProperty(),
						root.heightProperty()));
		possibleMoveIndicator.setFill(POSSIBLE_MOVE_DOT_COLOR);
		possibleMoveIndicator.setVisible(false);
		possibleMoveIndicator.setOpacity(0);

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

		pieceLabel = new Label();
		pieceLabel.setStyle("-fx-font-size: 28; -fx-font-weight: bold;");
		root.widthProperty().addListener((obs, oldV, newV) -> updatePieceSizing());
		root.heightProperty().addListener((obs, oldV, newV) -> updatePieceSizing());

		pieceImageView = new ImageView();
		pieceImageView.fitWidthProperty().bind(
				Bindings.createDoubleBinding(
						() -> Math.max(16, Math.min(root.getWidth(), root.getHeight()) * 0.82),
						root.widthProperty(),
						root.heightProperty()));
		pieceImageView.fitHeightProperty().bind(pieceImageView.fitWidthProperty());
		pieceImageView.setPreserveRatio(true);
		pieceImageView.setVisible(false);

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

		innerGlow = new InnerShadow();
		innerGlow.setColor(SELECTION_GLOW_COLOR);
		innerGlow.setRadius(25);
		innerGlow.setChoke(0.5);

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
		String nextSymbol = (pieceSymbol == null) ? "" : pieceSymbol;
		boolean pieceChanged = !currentPieceSymbol.equals(nextSymbol);
		currentPieceSymbol = nextSymbol;

		if (pieceChanged) {
			updatePieceRepresentation(nextSymbol);
		}

		handleSelection(isSelected);
		handlePossibleMoves(isPossibleMove, isCaptureMove);

		if (!isSelected && !isPossibleMove && !isCaptureMove) {
			updateBaseColor();
		}
	}

	private void updatePieceRepresentation(String pieceSymbol) {

		pieceLabel.setVisible(false);

		if (pieceSymbol == null || pieceSymbol.isEmpty()) {
			pieceImageView.setVisible(false);
			pieceImageView.setImage(null);
		} else {
			pieceImageView.setVisible(true);
			loadPieceImage(pieceSymbol);
		}
	}

	private void loadPieceImage(String pieceSymbol) {

		Image image = PieceImageLoader.loadPieceImageRaw(pieceSymbol);
		if (image == null || image.isError()) {
			if (DEBUG_IMAGES) {
				System.err.println("Image load failed for symbol: '" + pieceSymbol + "'");
			}
			pieceImageView.setVisible(false);
			pieceImageView.setImage(null);
			return;
		}

		pieceImageView.setImage(image);
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

		stopAllAnimations();

		cancelPendingTransitions();

		background.setEffect(null);

		Color baseColor = (position.getRow() + position.getCol()) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR;
		background.setFill(baseColor);

		if (selectionOverlay != null && selectionOverlay.isVisible()) {

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

			selectionOverlay.setFill(MOVE_HIGHLIGHT_COLOR);
			selectionOverlay.setVisible(true);
			selectionOverlay.setOpacity(1.0);

			InnerShadow highlightGlow = new InnerShadow();
			highlightGlow.setColor(Color.YELLOW);
			highlightGlow.setRadius(15);
			background.setEffect(highlightGlow);
		});
	}

	public void highlightAsHint() {
		javafx.application.Platform.runLater(() -> {

			selectionOverlay.setFill(HINT_HIGHLIGHT_COLOR);
			selectionOverlay.setVisible(true);
			selectionOverlay.setOpacity(1.0);

			InnerShadow hintGlow = new InnerShadow();
			hintGlow.setColor(Color.CYAN);
			hintGlow.setRadius(15);
			background.setEffect(hintGlow);
		});
	}

	public void clearHighlight() {

		if (piecePulseAnimation != null) {
			piecePulseAnimation.stop();
		}

		cancelPendingTransitions();

		isSelected = false;

		background.setEffect(null);
		selectionOverlay.setVisible(false);
		selectionOverlay.setFill(Color.TRANSPARENT);
		selectionOverlay.setOpacity(1.0);

		possibleMoveIndicator.setVisible(false);
		possibleMoveIndicator.setOpacity(0);
		captureIndicator.setVisible(false);
		captureIndicator.setScaleX(0);
		captureIndicator.setScaleY(0);

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

		updateBaseColor();
	}

	private void stopAllAnimations() {

		if (piecePulseAnimation != null) {
			piecePulseAnimation.stop();
		}

		if (selectionOverlay.getProperties().containsKey("currentFade")) {
			FadeTransition ft = (FadeTransition) selectionOverlay.getProperties().get("currentFade");
			if (ft != null)
				ft.stop();
		}

		if (pieceImageView.isVisible()) {
			pieceImageView.setScaleX(1.0);
			pieceImageView.setScaleY(1.0);
		}
		if (pieceLabel.isVisible()) {
			pieceLabel.setScaleX(1.0);
			pieceLabel.setScaleY(1.0);
		}
	}

	public javafx.scene.image.Image getPieceImage() {
		return pieceImageView.getImage();
	}

	public String getPieceSymbol() {
		return currentPieceSymbol;
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