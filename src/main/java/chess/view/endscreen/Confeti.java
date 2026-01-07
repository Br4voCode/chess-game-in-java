package chess.view.endscreen;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Lluvia simple de confeti (JavaFX).
 *
 * - Genera pequeñas piezas de distintos colores
 * - Caen desde la parte superior con ligera deriva lateral y rotación
 * - Pensado para usarse como capa visual en un StackPane
 */
public final class Confeti extends Pane {

	private static final int DEFAULT_PIECES = 90;
	private static final double GRAVITY_PX_PER_SEC = 260.0;
	private static final double SPAWN_TOP_PADDING = -40.0;
	private static final double DRAG = 0.995;
	private static final double FPS = 60.0;

	private final List<Piece> pieces = new ArrayList<>();
	private Timeline timeline;

	public Confeti() {
		setPickOnBounds(false);
		setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

		// Cuando el panel tenga tamaño, generamos confeti.
		widthProperty().addListener((obs, oldV, newV) -> ensurePieces());
		heightProperty().addListener((obs, oldV, newV) -> ensurePieces());
	}

	public void start() {
		ensurePieces();
		if (timeline != null && timeline.getStatus() == Animation.Status.RUNNING) {
			return;
		}

		final double dt = 1.0 / FPS;
		timeline = new Timeline(new KeyFrame(Duration.millis(1000.0 / FPS), e -> tick(dt)));
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
	}

	public void stop() {
		if (timeline != null) {
			timeline.stop();
			timeline = null;
		}
	}

	private void ensurePieces() {
		if (getWidth() <= 0 || getHeight() <= 0) {
			return;
		}
		if (!pieces.isEmpty()) {
			return;
		}
		spawn(DEFAULT_PIECES);
	}

	private void spawn(int count) {
		getChildren().clear();
		pieces.clear();

		ThreadLocalRandom r = ThreadLocalRandom.current();
		for (int i = 0; i < count; i++) {
			double w = r.nextDouble(5, 11);
			double h = r.nextDouble(6, 14);

			Rectangle rect = new Rectangle(w, h);
			rect.setArcWidth(r.nextDouble(1, 5));
			rect.setArcHeight(r.nextDouble(1, 5));
			rect.setFill(randomConfettiColor(r));
			rect.setOpacity(r.nextDouble(0.75, 0.98));

			double x = r.nextDouble(0, Math.max(1, getWidth() - w));
			double y = r.nextDouble(SPAWN_TOP_PADDING - getHeight() * 0.3, SPAWN_TOP_PADDING);
			rect.setTranslateX(x);
			rect.setTranslateY(y);
			rect.setRotate(r.nextDouble(0, 360));

			Piece p = new Piece(
					rect,
					r.nextDouble(-80, 80),
					r.nextDouble(40, 220),
					r.nextDouble(-220, 220));

			pieces.add(p);
			getChildren().add(rect);
		}
	}

	private void tick(double dtSeconds) {
		if (getWidth() <= 0 || getHeight() <= 0) {
			return;
		}

		ThreadLocalRandom r = ThreadLocalRandom.current();
		for (Piece p : pieces) {
			// Física básica
			p.vy += GRAVITY_PX_PER_SEC * dtSeconds;
			p.vx *= DRAG;
			p.vy *= DRAG;

			double x = p.node.getTranslateX() + p.vx * dtSeconds;
			double y = p.node.getTranslateY() + p.vy * dtSeconds;
			double rot = p.node.getRotate() + p.vr * dtSeconds;

			// Deriva suave aleatoria
			p.vx += r.nextDouble(-18, 18) * dtSeconds;

			// Wrap/respawn
			if (y > getHeight() + 30) {
				y = r.nextDouble(SPAWN_TOP_PADDING - 30, SPAWN_TOP_PADDING);
				x = r.nextDouble(0, Math.max(1, getWidth() - ((Rectangle) p.node).getWidth()));
				p.vy = r.nextDouble(40, 220);
				p.vx = r.nextDouble(-100, 100);
				p.vr = r.nextDouble(-260, 260);
				p.node.setOpacity(r.nextDouble(0.75, 0.98));
				((Rectangle) p.node).setFill(randomConfettiColor(r));
			}

			// Mantener dentro de bordes horizontales (rebote suave)
			if (x < -20) {
				x = -20;
				p.vx = Math.abs(p.vx);
			} else if (x > getWidth() + 20) {
				x = getWidth() + 20;
				p.vx = -Math.abs(p.vx);
			}

			p.node.setTranslateX(x);
			p.node.setTranslateY(y);
			p.node.setRotate(rot);
		}
	}

	private static Color randomConfettiColor(ThreadLocalRandom r) {
		// Paleta viva (evita colores demasiado oscuros)
		Color[] palette = new Color[] {
				Color.web("#ff4757"),
				Color.web("#ffa502"),
				Color.web("#2ed573"),
				Color.web("#1e90ff"),
				Color.web("#3742fa"),
				Color.web("#eccc68"),
				Color.web("#ff6b81"),
				Color.web("#7bed9f"),
				Color.web("#70a1ff"),
				Color.web("#a29bfe")
		};
		return palette[r.nextInt(palette.length)];
	}

	private static final class Piece {
		final javafx.scene.Node node;
		double vx;
		double vy;
		double vr;

		Piece(javafx.scene.Node node, double vx, double vy, double vr) {
			this.node = node;
			this.vx = vx;
			this.vy = vy;
			this.vr = vr;
		}
	}
}
