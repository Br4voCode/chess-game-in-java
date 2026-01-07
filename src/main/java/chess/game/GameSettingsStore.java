package chess.game;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Persists {@link GameSettings} so the app can restore the last played mode and preferences.
 */
public final class GameSettingsStore {
    private static final String SETTINGS_FILE = "game_settings.dat";

    private GameSettingsStore() {
    }

    public static void save(GameSettings settings) {
        if (settings == null) {
            return;
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SETTINGS_FILE))) {
            oos.writeObject(settings);
        } catch (IOException e) {
            System.err.println("Error saving game settings: " + e.getMessage());
        }
    }

    public static GameSettings loadOrDefault() {
        GameSettings loaded = load();
        if (loaded != null) {
            return loaded;
        }
        return GameSettings.humanVsAI(chess.model.PieceColor.WHITE, GameSettings.DEFAULT_DEPTH);
    }

    public static GameSettings load() {
        File file = new File(SETTINGS_FILE);
        if (!file.exists()) {
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SETTINGS_FILE))) {
            Object obj = ois.readObject();
            if (obj instanceof GameSettings) {
                return (GameSettings) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading game settings: " + e.getMessage());
        }
        return null;
    }
}
