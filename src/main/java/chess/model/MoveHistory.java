package chess.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona el historial de movimientos de una partida.
 * Permite guardar y cargar movimientos en un archivo .dat.
 */
public class MoveHistory {
    private List<Move> moves;
    private String filePath;
    
    /**
     * Constructor que inicializa un historial vacío
     * @param filePath la ruta del archivo donde se guardará el historial
     */
    public MoveHistory(String filePath) {
        this.moves = new ArrayList<>();
        this.filePath = filePath;
    }
    
    /**
     * Añade un movimiento al historial y lo guarda en el archivo
     * @param move el movimiento a añadir
     */
    public void addMove(Move move) {
        moves.add(move);
        saveToFile();
    }
    
    /**
     * Obtiene todos los movimientos del historial
     * @return lista de movimientos
     */
    public List<Move> getMoves() {
        return new ArrayList<>(moves);
    }
    
    /**
     * Obtiene el número de movimientos realizados
     * @return cantidad de movimientos
     */
    public int getMoveCount() {
        return moves.size();
    }
    
    /**
     * Obtiene un movimiento específico por su índice
     * @param index índice del movimiento (0-basado)
     * @return el movimiento en el índice especificado
     */
    public Move getMove(int index) {
        if (index >= 0 && index < moves.size()) {
            return moves.get(index);
        }
        return null;
    }
    
    /**
     * Limpia el historial de movimientos
     */
    public void clear() {
        moves.clear();
        saveToFile();
    }
    
    /**
     * Guarda el historial en el archivo .dat
     */
    public void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(filePath))) {
            oos.writeObject(moves);
        } catch (IOException e) {
            System.err.println("Error al guardar el historial: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Carga el historial desde el archivo .dat
     * @return true si se cargó correctamente, false en caso contrario
     */
    public boolean loadFromFile() {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("Archivo de historial no encontrado: " + filePath);
            moves.clear();
            return false;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(filePath))) {
            @SuppressWarnings("unchecked")
            List<Move> loadedMoves = (List<Move>) ois.readObject();
            this.moves = loadedMoves;
            System.out.println("Historial cargado correctamente: " + moves.size() + " movimientos.");
            return true;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error al cargar el historial: " + e.getMessage());
            e.printStackTrace();
            moves.clear();
            return false;
        }
    }
    
    /**
     * Obtiene una representación en texto del historial
     * @return string con todos los movimientos
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MoveHistory (").append(moves.size()).append(" moves):\n");
        for (int i = 0; i < moves.size(); i++) {
            int moveNumber = (i / 2) + 1;
            String color = (i % 2 == 0) ? "White" : "Black";
            sb.append(moveNumber).append(". ").append(color).append(": ")
                    .append(moves.get(i)).append("\n");
        }
        return sb.toString();
    }
}
