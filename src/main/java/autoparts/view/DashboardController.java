package autoparts.view;

import autoparts.model.User;
import autoparts.service.Session;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Controlador de la ventana principal (Dashboard.fxml): menú lateral y contenido.
 *
 * Cada módulo agrega su pantalla creando el archivo FXML con el nombre de abajo
 * (Catalog.fxml, Comparator.fxml, Partners.fxml, MyAccount.fxml). Mientras no exista,
 * se muestra "Sección en construcción".
 *
 * @author Jahdiel Torres
 */
public class DashboardController {

    public static final String HOME = "Home.fxml";
    public static final String CATALOG = "Catalog.fxml";
    public static final String COMPARATOR = "Comparator.fxml";
    public static final String PARTNERS = "Partners.fxml";
    public static final String MY_ACCOUNT = "MyAccount.fxml";

    @FXML
    private StackPane content;
    @FXML
    private Button btnHome;
    @FXML
    private Button btnCatalog;
    @FXML
    private Button btnComparator;
    @FXML
    private Button btnPartners;
    @FXML
    private Button btnMyAccount;
    @FXML
    private Label lblUser;
    @FXML
    private Label lblRole;

    private Button activeButton;

    /**
     * JavaFX llama este método automáticamente al cargar la pantalla.
     */
    @FXML
    private void initialize() {
        showUser();
        goToHome();
    }

    /**
     * Muestra quién inició sesión y oculta las opciones que su rol no puede usar.
     */
    private void showUser() {
        User user = Session.getUser();
        if (user == null) {
            return;
        }
        lblUser.setText(user.getName());
        lblRole.setText(user.getRole());
        if (User.ROLE_CLIENT.equals(user.getRole())) {
            btnPartners.setVisible(false);
            btnPartners.setManaged(false);
        }
    }

    @FXML
    private void goToHome() {
        show(HOME, btnHome);
    }

    @FXML
    private void goToCatalog() {
        show(CATALOG, btnCatalog);
    }

    @FXML
    private void goToComparator() {
        show(COMPARATOR, btnComparator);
    }

    @FXML
    private void goToPartners() {
        show(PARTNERS, btnPartners);
    }

    @FXML
    private void goToMyAccount() {
        show(MY_ACCOUNT, btnMyAccount);
    }

    @FXML
    private void logout() {
        Session.close();
        Navigator.goTo(Navigator.LOGIN);
    }

    /**
     * Muestra una vista en el centro de la ventana y resalta su botón del menú.
     */
    private void show(String view, Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("menu-active");
        }
        button.getStyleClass().add("menu-active");
        activeButton = button;

        if (Navigator.exists(view)) {
            content.getChildren().setAll(Navigator.load(view));
        } else {
            content.getChildren().setAll(underConstruction(view));
        }
    }

    /**
     * Crea el aviso que se muestra cuando una pantalla todavía no existe.
     */
    private VBox underConstruction(String view) {
        Label title = new Label("Sección en construcción");
        title.getStyleClass().add("section-title");
        Label detail = new Label("Esta pantalla aparecerá cuando se agregue " + view + ".");
        detail.getStyleClass().add("secondary-text");
        VBox box = new VBox(8, title, detail);
        box.setAlignment(Pos.CENTER);
        return box;
    }
}
