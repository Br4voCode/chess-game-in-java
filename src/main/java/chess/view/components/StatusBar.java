package chess.view.components;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Barra de estado en la parte inferior
 */
public class StatusBar {
    private HBox root;
    private Label statusLabel;

    public StatusBar() {
        initializeComponent();
    }

    private void initializeComponent() {
        root = new HBox();
        root.setStyle("-fx-background-color: #3c3f41; -fx-padding: 10; -fx-alignment: center-left;");
        
        statusLabel = new Label("White to move. Click a piece.");
        statusLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14;");
        
        HBox.setHgrow(statusLabel, Priority.ALWAYS);
        root.getChildren().add(statusLabel);
    }

    public void setStatus(String status) {
        statusLabel.setText(status);
    }

    public HBox getRoot() {
        return root;
    }
}