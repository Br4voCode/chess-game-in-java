package chess.view;

import chess.controller.GameController;
import chess.game.Game;
import chess.game.HumanPlayer;
import chess.game.AIPlayer;
import chess.model.PieceColor;
import chess.view.components.StatusBar;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;

public class GameView {
    private BorderPane root;
    private ChessBoard chessBoard;
    private StatusBar statusBar;
    private GameController controller;

    public GameView() {
        initializeComponents();
        setupLayout();
    }

    private void initializeComponents() {
        // Crear el modelo del juego
        Game game = new Game(new HumanPlayer(), new AIPlayer(PieceColor.BLACK, 3));
        
        // Crear componentes
        this.chessBoard = new ChessBoard();
        this.statusBar = new StatusBar();
        this.controller = new GameController(game, chessBoard, statusBar);
        
        // Configurar el controlador
        this.chessBoard.setSquareClickListener(controller::onSquareClicked);
    }

    private void setupLayout() {
        root = new BorderPane();
        
        // Tablero en el centro - siempre cuadrado
        VBox boardContainer = new VBox(chessBoard.getBoard());
        boardContainer.setAlignment(Pos.CENTER);
        boardContainer.setPadding(new Insets(20));
        
        // Hacer que el tablero sea responsive pero mantenga relación 1:1
        chessBoard.getBoard().setMaxSize(480, 480);
        chessBoard.getBoard().setMinSize(480, 480);
        
        // Espacio para futuros paneles
        VBox leftPanel = createSidePanel("Left Panel");
        VBox rightPanel = createSidePanel("Right Panel");
        
        root.setCenter(boardContainer);
        root.setLeft(leftPanel);
        root.setRight(rightPanel);
        root.setBottom(statusBar.getRoot());
        
        // Estilos
        root.setStyle("-fx-background-color: #2b2b2b;");
    }

    private VBox createSidePanel(String title) {
        VBox panel = new VBox();
        panel.setPrefWidth(150);
        panel.setStyle("-fx-background-color: #3c3f41; -fx-padding: 10;");
        panel.setVisible(false); // Ocultos por ahora, para futuras features
        
        return panel;
    }

    public BorderPane getRoot() {
        return root;
    }
}