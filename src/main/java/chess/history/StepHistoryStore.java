package chess.history;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistence for {@link StepHistory}.
 *
 * <p>
 * Policy (as per current requirements): we only persist the applied timeline (past).
 * If the user undoes and closes the game, the file reflects the state at the moment of closing.
 */
public class StepHistoryStore {
    private final String filePath;
    private GameMetadata gameMetadata;

    public StepHistoryStore(String filePath) {
        this.filePath = filePath;
        this.gameMetadata = null;
    }

    public void setGameMetadata(GameMetadata metadata) {
        this.gameMetadata = metadata;
    }

    public GameMetadata getGameMetadata() {
        return gameMetadata;
    }

    public String getFilePath() {
        return filePath;
    }

    public void saveApplied(StepHistory history) {
        List<Step> applied = history != null ? new ArrayList<>(history.getAppliedSteps()) : new ArrayList<>();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(gameMetadata);
            oos.writeObject(applied);
        } catch (IOException e) {
            System.err.println("Error al guardar el historial de steps: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Step> loadApplied() {
        File file = new File(filePath);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            Object first = ois.readObject();
            
            if (first instanceof GameMetadata) {
                // New format: metadata + list
                gameMetadata = (GameMetadata) first;
                @SuppressWarnings("unchecked")
                List<Step> loaded = (List<Step>) ois.readObject();
                return loaded != null ? loaded : new ArrayList<>();
            } else if (first instanceof List) {
                // Old format: just list (backward compatibility)
                gameMetadata = null;
                @SuppressWarnings("unchecked")
                List<Step> loaded = (List<Step>) first;
                return loaded != null ? loaded : new ArrayList<>();
            } else {
                System.err.println("Formato de archivo desconocido");
                return new ArrayList<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error al cargar el historial de steps: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
