package chess.view;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

import chess.util.GameLogger;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * A reusable log console component that displays timestamped log messages.
 */
public class LogConsole {
    private final TextArea logsArea;
    private final LinkedList<String> logBuffer;
    private final DateTimeFormatter timeFormatter;
    private static final int MAX_LOGS = 5000;

    public LogConsole() {
        logBuffer = new LinkedList<>();
        timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        
        logsArea = new TextArea();
        logsArea.setEditable(false);
        logsArea.setWrapText(true);
        logsArea.setStyle("-fx-control-inner-background: #1e1e1e; -fx-text-fill: #00ff00; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
        logsArea.setPrefHeight(200);
        
        // Register this console as a listener to the game logger
        GameLogger.getInstance().addListener(this::log);
    }

    /**
     * Get the TextArea component to add to a layout
     */
    public TextArea getLogsArea() {
        return logsArea;
    }

    /**
     * Add a log message with timestamp
     */
    public void log(String message) {
        Platform.runLater(() -> {
            String timestamp = LocalTime.now().format(timeFormatter);
            String logEntry = "[" + timestamp + "] " + message;
            
            logBuffer.addLast(logEntry);
            if (logBuffer.size() > MAX_LOGS) {
                logBuffer.removeFirst();
            }
            
            updateLogsDisplay();
        });
    }

    /**
     * Clear all logs
     */
    public void clear() {
        logBuffer.clear();
        logsArea.clear();
    }

    /**
     * Update the logs display area
     */
    private void updateLogsDisplay() {
        StringBuilder sb = new StringBuilder();
        for (String entry : logBuffer) {
            sb.append(entry).append("\n");
        }
        logsArea.setText(sb.toString());
        
        // Auto-scroll to the bottom
        logsArea.positionCaret(logsArea.getLength());
    }

    /**
     * Create a VBox container with the console for easy integration
     */
    public VBox createContainer(String title) {
        VBox container = new VBox(5);
        container.setPadding(new Insets(0));
        container.setStyle("-fx-background-color: #2b2b2b;");
        
        if (title != null && !title.isEmpty()) {
            javafx.scene.control.Label titleLabel = new javafx.scene.control.Label(title);
            titleLabel.setStyle("-fx-text-fill: #bbbbbb; -fx-font-size: 12px;");
            container.getChildren().add(titleLabel);
        }
        
        logsArea.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(logsArea, Priority.ALWAYS);
        container.getChildren().add(logsArea);
        VBox.setVgrow(container, Priority.ALWAYS);
        
        return container;
    }
}
