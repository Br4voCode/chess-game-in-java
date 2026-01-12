package chess.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class GameView {
    private BorderPane root;

    private LeftPanelView leftPanel;
    private BoardView boardView;
    private RightPanelView rightPanel;

    private Runnable onBackToMenu;

    public GameView() {
        build();
    }

    private void build() {
        root = new BorderPane();

        // Crear barra de título
        HBox titleBar = createTitleBar();

        leftPanel = new LeftPanelView();
        boardView = new BoardView();
        rightPanel = new RightPanelView();

        // Conectar botón "Back" del panel izquierdo
        leftPanel.onBack(() -> resetGame());

        root.setTop(titleBar);
        root.setLeft(leftPanel.getRoot());
        root.setCenter(boardView.getRoot());
        root.setRight(rightPanel.getRoot());

        root.setStyle("-fx-background-color: #2b2b2b;");
    }

    private HBox createTitleBar() {
        Label title = new Label("♔ Chess Game ♚");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox titleBar = new HBox(title);
        titleBar.setAlignment(Pos.CENTER);
        titleBar.setPadding(new Insets(10));
        titleBar.setStyle("-fx-background-color: #3c3f41;");

        return titleBar;
    }

    public BorderPane getRoot() {
        return root;
    }

    // getters SOLO para que el controller las conecte
    public LeftPanelView getLeftPanel() {
        return leftPanel;
    }

    public BoardView getBoardView() {
        return boardView;
    }

    public RightPanelView getRightPanel() {
        return rightPanel;
    }

    /**
     * Establecer callback para volver al menú
     */
    public void setOnBackToMenu(Runnable callback) {
        this.onBackToMenu = callback;
    }

    /**
     * Dispara el callback de volver al menú
     */
    public void triggerBackToMenu() {
        if (onBackToMenu != null) {
            onBackToMenu.run();
        }
    }

    /**
     * Resetea el GameView para una nueva partida
     * Limpia el estado del juego actual
     */
    public void resetGame() {
        // Limpiar paneles
        rightPanel.clearMovesHistory();
        rightPanel.clearCapturedPieces();

        // Resetear estados de UI
        leftPanel.setTurn("White");
        leftPanel.setGameState("In progress");
        leftPanel.enableUndo(false);
        leftPanel.enableRedo(false);

        // Llamar al callback para volver al menú
        triggerBackToMenu();
    }
}
