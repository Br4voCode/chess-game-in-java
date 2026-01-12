package chess.view;

import javafx.scene.layout.StackPane;

/**
 * AppView es el contenedor raíz de la aplicación.
 * Maneja el cambio entre StartScreen y GameView.
 * 
 * RESPONSABILIDAD:
 * - Mostrar/ocultar vistas
 * - El Controller decide qué vista mostrar
 */
public class AppView {
    private StackPane root;
    private StartScreen startScreen;
    private GameView gameView;

    public AppView() {
        root = new StackPane();
        initializeViews();
    }

    private void initializeViews() {
        startScreen = new StartScreen();
        gameView = new GameView();
    }

    /**
     * Muestra la pantalla de inicio
     */
    public void showStartScreen() {
        root.getChildren().clear();
        root.getChildren().add(startScreen.getRoot());
    }

    /**
     * Muestra la pantalla de juego
     */
    public void showGameView() {
        root.getChildren().clear();
        root.getChildren().add(gameView.getRoot());
    }

    /**
     * Retorna el root de la aplicación (para colocarlo en el Stage)
     */
    public StackPane getRoot() {
        return root;
    }

    /**
     * Acceso al StartScreen para el Controller
     */
    public StartScreen getStartScreen() {
        return startScreen;
    }

    /**
     * Acceso al GameView para el Controller
     */
    public GameView getGameView() {
        return gameView;
    }
}