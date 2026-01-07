package chess.view.endscreen;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Pop-up modal para el final de partida con 3 variantes: WIN, LOSE y DRAW.
 *
 * Características comunes:
 * - Ventana tipo pop-up (sin bordes) y modal.
 * - Botón "Aceptar" (por ahora solo cierra).
 *
 * Variantes:
 * - WIN: rey verde + confeti.
 * - LOSE: rey rojo.
 * - DRAW: símbolo azul de empate.
 */
public final class GameEndScreen {

	public enum Result {
		WIN,
		LOSE,
		DRAW
	}

	private static final int DEFAULT_WIDTH = 520;
	private static final int DEFAULT_HEIGHT = 320;

	private final Stage stage;
	private final StackPane root;
	private final VBox card;
	private final Confeti confeti;
	private final String customMessage;

	public GameEndScreen(Stage owner, Result result) {
		this(owner, result, null);
	}

	public GameEndScreen(Stage owner, Result result, String customMessage) {
		this.customMessage = customMessage;
		this.stage = new Stage(StageStyle.TRANSPARENT);
		this.stage.initOwner(owner);
		this.stage.initModality(Modality.WINDOW_MODAL);
		this.stage.setResizable(false);

		this.confeti = new Confeti();
		this.confeti.setMouseTransparent(true);

		this.card = new VBox(14);
		this.card.setPadding(new Insets(18));
		this.card.setAlignment(Pos.CENTER);
		this.card.setMaxWidth(DEFAULT_WIDTH);
		this.card.setEffect(new DropShadow(30, Color.rgb(0, 0, 0, 0.55)));

		this.card.setStyle(
				"-fx-background-radius: 18;" +
						"-fx-border-radius: 18;" +
						"-fx-border-width: 1;" +
						"-fx-border-color: rgba(255,255,255,0.12);" +
						"-fx-background-color: rgba(25,25,28,0.92);");

		this.root = new StackPane();
		this.root.setPadding(new Insets(18));
		this.root.getChildren().add(card);

		this.root.setStyle("-fx-background-color: rgba(0,0,0,0.55);");

		Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
		scene.setFill(Color.TRANSPARENT);
		this.stage.setScene(scene);

		applyVariant(result);

		scene.setOnKeyPressed(e -> {
			switch (e.getCode()) {
				case ESCAPE -> close();
				default -> {
				}
			}
		});
	}

	public void show() {
		stage.show();
	}

	public void close() {
		confeti.stop();
		stage.close();
	}

	public Stage getStage() {
		return stage;
	}

	private void applyVariant(Result result) {
		card.getChildren().clear();

		VariantVisual visual = VariantVisual.from(result);

		Label icon = new Label(visual.icon);
		icon.setTextFill(visual.accent);
		icon.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 72));
		icon.setEffect(new DropShadow(30, Color.rgb(0, 0, 0, 0.35)));

		Label title = new Label(visual.title);
		title.setTextFill(Color.WHITE);
		title.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 28));

		String subtitleText = customMessage != null ? customMessage : visual.subtitle;
		Label subtitle = new Label(subtitleText);
		subtitle.setTextFill(Color.rgb(220, 220, 225));
		subtitle.setFont(Font.font("System", FontWeight.NORMAL, 14));
		subtitle.setWrapText(true);
		subtitle.setMaxWidth(420);
		subtitle.setAlignment(Pos.CENTER);

		Region spacer = new Region();
		VBox.setVgrow(spacer, Priority.ALWAYS);

		Button accept = new Button("Aceptar");
		accept.setDefaultButton(true);
		accept.setOnAction(e -> close());
		accept.setStyle(
				"-fx-background-radius: 12;" +
						"-fx-padding: 10 18;" +
						"-fx-font-size: 14;" +
						"-fx-font-weight: bold;" +
						"-fx-text-fill: white;" +
						"-fx-background-color: " + toRgbaCss(visual.accent.deriveColor(0, 1, 1, 0.95)) + ";");

		HBox footer = new HBox(accept);
		footer.setAlignment(Pos.CENTER);

		Region accentBar = new Region();
		accentBar.setPrefHeight(6);
		accentBar.setMaxWidth(Double.MAX_VALUE);
		accentBar.setStyle(
				"-fx-background-radius: 14;" +
						"-fx-background-color: " + toRgbaCss(visual.accent.deriveColor(0, 1, 1, 0.9)) + ";");

		card.getChildren().addAll(accentBar, icon, title, subtitle, spacer, footer);

		root.getChildren().remove(confeti);
		if (result == Result.WIN) {
			if (!root.getChildren().contains(confeti)) {
				root.getChildren().add(0, confeti);
			}

			confeti.prefWidthProperty().bind(root.widthProperty());
			confeti.prefHeightProperty().bind(root.heightProperty());
			confeti.start();
		} else {
			confeti.stop();
		}
	}

	private static String toRgbaCss(Color c) {
		int r = (int) Math.round(c.getRed() * 255);
		int g = (int) Math.round(c.getGreen() * 255);
		int b = (int) Math.round(c.getBlue() * 255);
		return String.format("rgba(%d,%d,%d,%.3f)", r, g, b, c.getOpacity());
	}

	private record VariantVisual(String title, String subtitle, String icon, Color accent) {
		static VariantVisual from(Result result) {
			return switch (result) {
				case WIN -> new VariantVisual(
						"¡Victoria!",
						"Buen juego. El rey está a salvo… y la celebración empieza.",
						"♔",
						Color.web("#2ecc71"));
				case LOSE -> new VariantVisual(
						"Derrota",
						"Esta vez no pudo ser. Respira, aprende y vuelve a intentarlo.",
						"♚",
						Color.web("#e74c3c"));
				case DRAW -> new VariantVisual(
						"Empate",
						"Nadie cede terreno. Una batalla equilibrada.",
						"≋",
						Color.web("#3498db"));
			};
		}
	}
}
