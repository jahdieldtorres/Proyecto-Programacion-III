package autoparts.view;

import autoparts.model.CatalogItem;
import autoparts.model.User;
import autoparts.service.Session;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Controlador de la ventana principal (Dashboard.fxml): barra superior y contenido.
 *
 * Cada módulo se conecta por el nombre de su archivo FXML (constantes de abajo).
 * Mientras el archivo no exista, se muestra "Sección en construcción". La barra
 * se ajusta al rol de quien inició sesión: el menú Administrar (repuestos,
 * vehículos, socios, inventario, tarifas, afiliaciones y usuarios) solo lo ve
 * el admin.
 *
 * Si la sesión es de invitado, el menú del usuario dice "Invitado" y ofrece
 * Registrarme en vez de Mi cuenta, y el carrito no se muestra.
 *
 * @author Jahdiel Torres
 * @author Héctor Guevara (modo invitado)
 */
public class DashboardController {

    public static final String HOME = "Home.fxml";
    public static final String SHOP = "Shop.fxml";
    public static final String DETAIL = "ComponentDetail.fxml";
    public static final String CATALOG = "Catalog.fxml";
    public static final String COMPARATOR = "Comparator.fxml";
    public static final String VEHICLES = "Vehicles.fxml";
    public static final String SERVICES = "Services.fxml";
    public static final String MAP = "Map.fxml";
    public static final String CART = "Cart.fxml";
    public static final String PARTNERS = "Partners.fxml";
    public static final String INVENTORY = "Inventory.fxml";
    public static final String FEES = "Fees.fxml";
    public static final String AFFILIATIONS = "Affiliations.fxml";
    public static final String USERS = "Users.fxml";
    public static final String MY_ACCOUNT = "MyAccount.fxml";

    /** Margen de las pantallas normales. El Inicio ocupa todo el ancho, sin margen. */
    private static final Insets PAGE_PADDING = new Insets(24, 28, 24, 28);

    /** Ventana principal abierta, para que otras pantallas puedan pedirle cambiar de sección. */
    private static DashboardController current;

    @FXML
    private StackPane content;
    @FXML
    private TextField searchField;
    @FXML
    private Button btnHome;
    @FXML
    private Button btnCatalog;
    @FXML
    private Button btnServices;
    @FXML
    private Button btnMap;
    @FXML
    private Button btnCart;
    @FXML
    private MenuButton menuAdmin;
    @FXML
    private MenuButton menuUser;
    @FXML
    private MenuItem itemMyAccount;
    @FXML
    private MenuItem itemRegister;
    @FXML
    private Label lblCartCount;

    /** Opción de la barra que está resaltada (la sección abierta). */
    private Node activeItem;

    /**
     * JavaFX llama este método automáticamente al cargar la pantalla.
     */
    @FXML
    private void initialize() {
        current = this;
        showUser();
        goToHome();
    }

    /**
     * Abre el catálogo con un texto ya buscado (vacío = todo el catálogo).
     * Lo usan el buscador de la barra, Inicio y los botones "Volver".
     *
     * @param text lo que el usuario escribió
     */
    public static void openCatalogWith(String text) {
        ShopController.setPendingSearch(text);
        if (current != null) {
            current.show(SHOP, current.btnCatalog);
        }
    }

    /**
     * Abre el detalle de un repuesto con la comparación entre almacenes.
     *
     * @param item el repuesto elegido en el catálogo o en Inicio
     */
    public static void openDetail(CatalogItem item) {
        ComponentDetailController.setItem(item);
        if (current != null) {
            current.show(DETAIL, current.btnCatalog);
        }
    }

    /**
     * Abre "Talleres cerca" con el mapa. La usa la sección "Cerca de ti" de Inicio.
     */
    public static void openMap() {
        if (current != null) {
            current.goToMap();
        }
    }

    /**
     * Abre la pantalla de servicios. La usa el recuadro de talleres de Inicio.
     */
    public static void openServices() {
        if (current != null) {
            current.goToServices();
        }
    }

    /**
     * Actualiza el número del carrito en la barra. Lo llaman las pantallas
     * que agregan o quitan repuestos del carrito.
     */
    public static void refreshCartCount() {
        if (current != null) {
            current.lblCartCount.setText(String.valueOf(Session.getCart().getUnitCount()));
        }
    }

    /**
     * Muestra el nombre de quien inició sesión y oculta el menú Administrar
     * si no es administrador. Si es invitado, muestra "Invitado", ofrece
     * Registrarme y oculta el carrito.
     */
    private void showUser() {
        if (Session.isGuest()) {
            showGuest();
            return;
        }
        User user = Session.getUser();
        if (user == null) {
            return;
        }
        itemRegister.setVisible(false);
        menuUser.setText(firstName(user.getName()));
        boolean admin = Session.isAdmin();
        menuAdmin.setVisible(admin);
        menuAdmin.setManaged(admin); // setManaged(false) también quita el espacio que ocupa
        refreshCartCount();
    }

    /**
     * Barra para el invitado: sin Administrar ni carrito, y con Registrarme
     * en lugar de Mi cuenta.
     */
    private void showGuest() {
        menuUser.setText("Invitado");
        itemMyAccount.setVisible(false);
        itemRegister.setVisible(true);
        menuAdmin.setVisible(false);
        menuAdmin.setManaged(false);
        btnCart.setVisible(false);
        btnCart.setManaged(false);
    }

    /**
     * Devuelve solo el primer nombre, para que quepa en la barra.
     */
    private String firstName(String fullName) {
        String[] parts = fullName.trim().split(" ");
        return parts[0];
    }

    @FXML
    private void search() {
        openCatalogWith(searchField.getText());
        searchField.clear();
    }

    @FXML
    private void goToHome() {
        show(HOME, btnHome);
    }

    @FXML
    private void goToCatalog() {
        openCatalogWith("");
    }

    @FXML
    private void goToComponents() {
        show(CATALOG, menuAdmin);
    }

    @FXML
    private void goToVehicles() {
        show(VEHICLES, menuAdmin);
    }

    @FXML
    private void goToComparator() {
        show(COMPARATOR, menuAdmin);
    }

    @FXML
    private void goToServices() {
        show(SERVICES, btnServices);
    }

    @FXML
    private void goToMap() {
        show(MAP, btnMap);
    }

    @FXML
    private void goToCart() {
        if (Session.isGuest()) {
            return; // el invitado no tiene carrito
        }
        show(CART, btnCart);
    }

    @FXML
    private void goToPartners() {
        show(PARTNERS, menuAdmin);
    }

    @FXML
    private void goToInventory() {
        show(INVENTORY, menuAdmin);
    }

    @FXML
    private void goToFees() {
        show(FEES, menuAdmin);
    }

    @FXML
    private void goToAffiliations() {
        show(AFFILIATIONS, menuAdmin);
    }

    @FXML
    private void goToUsers() {
        show(USERS, menuAdmin);
    }

    @FXML
    private void goToMyAccount() {
        if (Session.isGuest()) {
            return; // el invitado no tiene cuenta
        }
        show(MY_ACCOUNT, menuUser);
    }

    /**
     * Opción Registrarme del invitado: cierra la sesión de invitado y abre el registro.
     */
    @FXML
    private void goToRegister() {
        openRegister();
    }

    /**
     * Cierra la sesión de invitado y lleva a la pantalla de registro. También
     * la usa la ventana "Regístrate para comprar" (GuestDialog).
     */
    public static void openRegister() {
        Session.close();
        current = null;
        Navigator.goTo(Navigator.REGISTER);
    }

    @FXML
    private void logout() {
        Session.close();
        current = null;
        Navigator.goTo(Navigator.LOGIN);
    }

    /**
     * Muestra una vista en el centro de la ventana, resalta su opción de la
     * barra y la hace aparecer con un desvanecido corto.
     *
     * @param view     nombre del archivo FXML
     * @param navItem  botón o menú de la barra que corresponde a esa vista
     */
    private void show(String view, Node navItem) {
        markActive(navItem);
        content.setPadding(HOME.equals(view) ? Insets.EMPTY : PAGE_PADDING);

        Parent screen = Navigator.exists(view) ? Navigator.load(view) : underConstruction(view);
        content.getChildren().setAll(screen);
        fadeIn(screen);
        refreshCartCount();
    }

    /**
     * Quita el resaltado de la opción anterior y lo pone en la nueva.
     */
    private void markActive(Node navItem) {
        if (activeItem != null) {
            activeItem.getStyleClass().remove("nav-active");
        }
        navItem.getStyleClass().add("nav-active");
        activeItem = navItem;
    }

    /**
     * Animación de entrada: la pantalla pasa de transparente a visible.
     */
    private void fadeIn(Node screen) {
        FadeTransition fade = new FadeTransition(Duration.millis(220), screen);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
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
