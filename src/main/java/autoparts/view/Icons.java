package autoparts.view;

import java.util.Map;
import javafx.scene.shape.SVGPath;

/**
 * Íconos de línea de la app, dibujados con trazos SVG de 24×24.
 *
 * Así no se necesitan archivos de imagen para cada ícono: el color y el grosor
 * se ponen en styles.css con la clase "icon".
 *
 * @author Jahdiel Torres
 */
public final class Icons {

    public static final String COMPARE = "M4 18V9M10 18V5M16 18v-7M22 18H2";
    public static final String LOCATION = "M12 21s-7-6.2-7-11.5a7 7 0 0 1 14 0C19 14.8 12 21 12 21zM12 7a2.5 2.5 0 1 0 0 5a2.5 2.5 0 1 0 0-5";
    public static final String WRENCH = "M14.5 6.5a4 4 0 0 0-5.3 5.3L4 17l3 3 5.2-5.2a4 4 0 0 0 5.3-5.3l-2.5 2.5-2.5-.5-.5-2.5z";
    public static final String PART = "M12 3l8 4.5v9L12 21l-8-4.5v-9zM12 12l8-4.5M12 12v9M12 12L4 7.5";

    /** Ícono de cada categoría del catálogo, por su nombre. */
    private static final Map<String, String> CATEGORIES = Map.of(
            "Frenos", "M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0-18M12 9a3 3 0 1 0 0 6a3 3 0 1 0 0-6",
            "Motor", "M5 8h12v9H5zM9 8V5h4v3M17 11h3v4h-3M5 12H3",
            "Llantas", "M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0-18M12 7.5a4.5 4.5 0 1 0 0 9a4.5 4.5 0 1 0 0-9",
            "Rines", "M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0-18M12 3v7M12 14v7M3 12h7M14 12h7",
            "Transmisión", "M7 4.5a2.5 2.5 0 1 0 0 5a2.5 2.5 0 1 0 0-5M17 4.5a2.5 2.5 0 1 0 0 5a2.5 2.5 0 1 0 0-5"
                    + "M12 14.5a2.5 2.5 0 1 0 0 5a2.5 2.5 0 1 0 0-5M7 9.5V12h10V9.5M12 12v2.5",
            "Suspensión", "M8 3h8M8 21h8M9 5l6 2.5L9 10l6 2.5L9 15l6 2.5",
            "Sistema eléctrico", "M13 2.5L5 13h6l-1 8.5 8-11h-6z");

    private Icons() {
    }

    /**
     * Crea un ícono a partir de su trazo.
     *
     * @param path trazo SVG, por ejemplo Icons.COMPARE
     * @return el ícono listo para ponerlo en la pantalla
     */
    public static SVGPath create(String path) {
        SVGPath icon = new SVGPath();
        icon.setContent(path);
        icon.getStyleClass().add("icon");
        return icon;
    }

    /**
     * Ícono de una categoría. Si la categoría no tiene uno propio, usa el de repuesto.
     *
     * @param categoryName nombre de la categoría
     * @return el ícono de esa categoría
     */
    public static SVGPath forCategory(String categoryName) {
        return create(CATEGORIES.getOrDefault(categoryName, PART));
    }
}
