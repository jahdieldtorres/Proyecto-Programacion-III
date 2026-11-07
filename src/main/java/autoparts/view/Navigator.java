package autoparts.view;

import java.io.IOException;
import java.util.function.Consumer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Se encarga de cambiar de pantalla y de abrir formularios, para no repetir
 * ese código en cada controlador (fabricación pura).
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

    /**
     * Abre un formulario en una ventana encima de la principal y espera a que
     * se cierre. Antes de mostrarlo le entrega sus datos al controlador del
     * formulario, por ejemplo el socio que se va a editar.
     *
     * Uso: {@code Navigator.openForm("PartnerForm.fxml", "Editar socio",
     * (PartnerFormController form) -> form.setPartner(socio));}
     *
     * @param view  nombre del archivo FXML del formulario
     * @param title texto de la barra de la ventana
     * @param setup recibe el controlador del formulario para pasarle los datos
     * @param <C>   tipo del controlador del formulario
     */
    public static <C> void openForm(String view, String title, Consumer<C> setup) {
        try {
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource(view));
            Parent root = loader.load();
            C controller = loader.getController();
            setup.accept(controller);

            Stage form = new Stage();
            form.setTitle(title);
            form.initOwner(window);
            form.initModality(Modality.WINDOW_MODAL); // bloquea la ventana de atrás
            form.setResizable(false);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Navigator.class.getResource("styles.css").toExternalForm());
            form.setScene(scene);
            form.showAndWait(); // el código sigue cuando el formulario se cierra
        } catch (IOException e) {
            throw new RuntimeException("No se pudo abrir el formulario " + view, e);
        }
    }

    /**
     * Cierra la ventana del formulario al que pertenece el componente indicado.
     *
     * @param anyNode cualquier componente del formulario, por ejemplo el botón Guardar
     */
    public static void closeForm(Node anyNode) {
        ((Stage) anyNode.getScene().getWindow()).close();
    }
}
