package autoparts.view;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Se encarga de cambiar de pantalla, para no repetir ese código en cada controlador.
 *
 * Para cambiar solo el contenido del centro de la ventana principal se usa
 * DashboardController.
 *
 * @author Jahdiel Torres
 */
public class Navigator {

    public static final String LOGIN = "Login.fxml";
    public static final String DASHBOARD = "Dashboard.fxml";
    public static final String REGISTER = "Register.fxml";

    private static Stage window;

    /**
     * Guarda la ventana de la aplicación para poder cambiar su contenido después.
     */
    public static void init(Stage mainWindow) {
        window = mainWindow;
    }

    /**
     * Reemplaza toda la pantalla por la vista indicada.
     *
     * @param view nombre del archivo FXML, por ejemplo Navigator.DASHBOARD
     */
    public static void goTo(String view) {
        Parent root = load(view);
        if (window.getScene() == null) {
            // La primera vez se crea la escena y se le aplican los estilos
            Scene scene = new Scene(root, 1280, 760);
            scene.getStylesheets().add(Navigator.class.getResource("styles.css").toExternalForm());
            window.setScene(scene);
        } else {
            window.getScene().setRoot(root);
        }
    }

    /**
     * Lee un archivo FXML y devuelve la pantalla que describe.
     *
     * @param view nombre del archivo FXML
     * @return el componente principal (raíz) de esa pantalla
     */
    public static Parent load(String view) {
        try {
            return FXMLLoader.load(Navigator.class.getResource(view));
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar la vista " + view, e);
        }
    }

    /**
     * Indica si ya existe el archivo FXML de una vista.
     */
    public static boolean exists(String view) {
        return Navigator.class.getResource(view) != null;
    }
}
