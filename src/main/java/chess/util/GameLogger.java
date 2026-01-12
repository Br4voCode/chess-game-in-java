package chess.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized logging system for game events and AI processing.
 * Supports multiple listeners that are notified of log events.
 */
public class GameLogger {
    private static GameLogger instance;
    private final List<LogListener> listeners = new ArrayList<>();

    public interface LogListener {
        void onLogMessage(String message);
    }

    private GameLogger() {
    }

    public static synchronized GameLogger getInstance() {
        if (instance == null) {
            instance = new GameLogger();
        }
        return instance;
    }

    /**
     * Subscribe to log messages
     */
    public void addListener(LogListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Unsubscribe from log messages
     */
    public void removeListener(LogListener listener) {
        listeners.remove(listener);
    }

    /**
     * Clear all listeners
     */
    public void clearListeners() {
        listeners.clear();
    }

    /**
     * Log a message to all listeners
     */
    public void log(String message) {
        
        System.out.println(message);
        
        
        for (LogListener listener : listeners) {
            listener.onLogMessage(message);
        }
    }

    /**
     * Log a formatted message
     */
    public void log(String format, Object... args) {
        log(String.format(format, args));
    }
}
