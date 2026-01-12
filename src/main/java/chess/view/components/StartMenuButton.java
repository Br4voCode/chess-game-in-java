package chess.view.components;

import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

/**
 * Componente de botón reutilizable para el menú de inicio
 * Con soporte para colores dinámicos (hex), efectos hover y click
 */
public class StartMenuButton extends Button {

    private static final String ENABLED_BUTTON_STYLE_TEMPLATE = "-fx-padding: 15px 40px; " +
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5; " +
            "-fx-background-color: %s;";

    private static final String DISABLED_BUTTON_STYLE = "-fx-padding: 15px 40px; " +
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: default; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5; " +
            "-fx-opacity: 1.0; " +
            "-fx-background-color: #888888; " +
            "-fx-text-fill: #cccccc;";

    private String baseColorHex;

    /**
     * Constructor con todas las configuraciones
     * 
     * @param text     Texto del botón
     * @param colorHex Color base del botón en formato hex (#RRGGBB)
     * @param minWidth Ancho mínimo del botón
     * @param enabled  Si el botón está habilitado
     */
    public StartMenuButton(String text, String colorHex, double minWidth, boolean enabled) {
        super(text);
        this.baseColorHex = colorHex;

        setMinWidth(minWidth);
        setStyle(enabled);
        setupEventHandlers();
    }

    /**
     * Constructor simplificado con ancho por defecto (250px)
     */
    public StartMenuButton(String text, String colorHex, boolean enabled) {
        this(text, colorHex, 250, enabled);
    }

    /**
     * Constructor básico con ancho por defecto y habilitado por defecto
     */
    public StartMenuButton(String text, String colorHex) {
        this(text, colorHex, 250, true);
    }

    /**
     * Configura los manejadores de eventos para hover y click
     */
    private void setupEventHandlers() {
        setOnMouseEntered((MouseEvent event) -> {
            if (!isDisabled()) {
                applyColorStyle(lightenColor(baseColorHex));
            }
        });

        setOnMouseExited((MouseEvent event) -> {
            if (!isDisabled()) {
                applyColorStyle(baseColorHex);
            }
        });

        setOnMousePressed((MouseEvent event) -> {
            if (!isDisabled()) {
                applyColorStyle(darkenColor(baseColorHex));
            }
        });

        setOnMouseReleased((MouseEvent event) -> {
            if (!isDisabled()) {
                applyColorStyle(baseColorHex);
            }
        });
    }

    /**
     * Aplica un color específico al botón
     */
    private void applyColorStyle(String hexColor) {
        setStyle(ENABLED_BUTTON_STYLE_TEMPLATE.replace("%s", hexColor) + " -fx-text-fill: white;");
    }

    /**
     * Aclara un color hex restando valor a sus componentes
     */
    private String lightenColor(String hexColor) {
        return adjustColorBrightness(hexColor, 0.2f); // 20% más claro
    }

    /**
     * Oscurece un color hex restando valor a sus componentes
     */
    private String darkenColor(String hexColor) {
        return adjustColorBrightness(hexColor, -0.2f); // 20% más oscuro
    }

    /**
     * Ajusta el brillo de un color hex
     * 
     * @param hexColor Color en formato #RRGGBB
     * @param factor   Factor de ajuste (-1 a 1), positivo aclara, negativo oscurece
     * @return Color ajustado en formato hex
     */
    private String adjustColorBrightness(String hexColor, float factor) {
        // Remover el # si existe
        String hex = hexColor.startsWith("#") ? hexColor.substring(1) : hexColor;

        // Convertir hex a RGB
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);

        // Ajustar brillo
        r = Math.min(255, (int) (r + (factor > 0 ? (255 - r) * factor : r * Math.abs(factor))));
        g = Math.min(255, (int) (g + (factor > 0 ? (255 - g) * factor : g * Math.abs(factor))));
        b = Math.min(255, (int) (b + (factor > 0 ? (255 - b) * factor : b * Math.abs(factor))));

        // Convertir de vuelta a hex
        return String.format("#%02X%02X%02X", r, g, b);
    }

    /**
     * Establece el estilo del botón basado en si está habilitado
     */
    private void setStyle(boolean enabled) {
        if (enabled) {
            applyColorStyle(baseColorHex);
            setDisable(false);
        } else {
            setStyle(DISABLED_BUTTON_STYLE);
            setDisable(true);
        }
    }

    /**
     * Cambia el estado del botón entre habilitado y deshabilitado
     */
    public void setEnabled(boolean enabled) {
        setStyle(enabled);
    }
}
