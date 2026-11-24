package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.PersistenceException;
import autoparts.model.Cart;
import autoparts.model.CartItem;
import autoparts.model.Component;
import autoparts.service.Session;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Controlador del carrito (Cart.fxml).
 *
 * Muestra los repuestos agrupados por almacén, porque cada almacén cobra su
 * propio envío. Deja cambiar cantidades, quitar repuestos y confirmar el
 * pedido. Las cuentas (subtotales, envíos y total) las hace la clase Cart.
 *
 * @author Jahdiel Torres
 */
public class CartController {

    @FXML
    private Label lblCount;
    @FXML
    private ScrollPane scroll;
    @FXML
    private VBox groupsBox;
    @FXML
    private VBox emptyBox;
    @FXML
    private Label lblUnits;
    @FXML
    private Label lblSubtotal;
    @FXML
    private Label lblStores;
    @FXML
    private Label lblShipping;
    @FXML
    private Label lblTotal;
    @FXML
    private Button btnConfirm;
    @FXML
    private Label lblMessage;

    /** Nombre de cada repuesto por su id, para mostrarlo en las filas. */
    private final Map<Integer, String> componentNames = new HashMap<>();

    /**
     * JavaFX llama este método automáticamente al cargar la pantalla.
     */
    @FXML
    private void initialize() {
        loadComponentNames();
        refresh();
    }

    /**
     * Vuelve a dibujar el carrito y el resumen. Se llama después de cada cambio.
     */
    private void refresh() {
        Cart cart = Session.getCart();
        groupsBox.getChildren().clear();
        for (List<CartItem> group : groupBySupplier(cart).values()) {
            groupsBox.getChildren().add(supplierCard(group, cart));
        }

        boolean empty = cart.isEmpty();
        emptyBox.setVisible(empty);
        scroll.setVisible(!empty);
        btnConfirm.setDisable(empty);

        int stores = cart.getShippingBySupplier().size();
        lblCount.setText(cart.getUnitCount() + " unidades de " + Formats.stores(stores));
        lblUnits.setText("Repuestos (" + cart.getUnitCount() + " unidades)");
        lblSubtotal.setText(Formats.money(cart.getSubtotal()));
        lblStores.setText("Envíos (" + Formats.stores(stores) + ")");
        lblShipping.setText(Formats.money(cart.getShippingTotal()));
        lblTotal.setText(Formats.money(cart.getTotal()));
        DashboardController.refreshCartCount();
    }

    /**
     * Agrupa las líneas del carrito por almacén, en el orden en que se agregaron.
     */
    private Map<Integer, List<CartItem>> groupBySupplier(Cart cart) {
        Map<Integer, List<CartItem>> groups = new LinkedHashMap<>();
        for (CartItem item : cart.getItems()) {
            groups.computeIfAbsent(item.getSupplierId(), id -> new ArrayList<>()).add(item);
        }
        return groups;
    }

    /**
     * Tarjeta de un almacén: encabezado con su nombre y envío, y una fila por repuesto.
     */
    private VBox supplierCard(List<CartItem> group, Cart cart) {
        CartItem first = group.get(0);
        Label name = new Label(first.getOffer().getSupplierName());
        name.getStyleClass().add("offer-supplier");
        Label shipping = new Label("Envío " + Formats.money(cart.getShippingBySupplier().get(first.getSupplierId()))
                + " · entrega en " + first.getOffer().getDeliveryDays() + " días");
        shipping.getStyleClass().add("secondary-text");
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox header = new HBox(name, space, shipping);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("cart-group-header");

        VBox card = new VBox(header);
        card.getStyleClass().add("card");
        for (CartItem item : group) {
            card.getChildren().add(itemRow(item, cart));
        }
        return card;
    }

    /**
     * Fila de un repuesto: nombre, precio, cantidad, subtotal y botón para quitarlo.
     */
    private HBox itemRow(CartItem item, Cart cart) {
        Label name = new Label(componentNames.getOrDefault(item.getOffer().getItem().getComponentId(), "Repuesto"));
        name.getStyleClass().add("product-name");
        Label detail = new Label(Formats.money(item.getUnitPrice()) + " c/u · "
                + Formats.condition(item.getOffer().getCondition())
                + " · " + item.getOffer().getStock() + " disponibles");
        detail.getStyleClass().add("secondary-text");
        VBox info = new VBox(3, name, detail);

        Spinner<Integer> quantity = new Spinner<>(1, item.getOffer().getStock(), item.getQuantity());
        quantity.setPrefWidth(80);
        quantity.valueProperty().addListener((observable, oldValue, newValue) -> {
            cart.setQuantity(item.getInventoryId(), newValue);
            refresh();
        });

        Label subtotal = new Label(Formats.money(item.getSubtotal()));
        subtotal.getStyleClass().add("product-price");
        subtotal.setMinWidth(110);
        subtotal.setAlignment(Pos.CENTER_RIGHT);

        Button remove = new Button("Quitar");
        remove.getStyleClass().add("link-button-danger");
        remove.setOnAction(event -> {
            cart.remove(item.getInventoryId());
            refresh();
        });

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox row = new HBox(16, info, space, quantity, subtotal, remove);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("cart-row");
        return row;
    }

    /**
     * Confirma el pedido. Se conecta con OrderService (módulo de pedidos de
     * Héctor) cuando ese módulo se fusione: ahí esta línea pasa a ser
     * {@code AppServices.orders().checkout(Session.getCart())}.
     */
    @FXML
    private void confirm() {
        lblMessage.getStyleClass().removeAll("error-message", "success-message");
        lblMessage.getStyleClass().add("success-message");
        lblMessage.setText("Pedido listo para confirmar. Esta opción se conecta con el módulo de pedidos.");
    }

    @FXML
    private void goToCatalog() {
        DashboardController.openCatalogWith("");
    }

    /**
     * Lee una vez los repuestos para poder mostrar su nombre en cada fila.
     */
    private void loadComponentNames() {
        try {
            for (Component component : AppServices.components().findAll()) {
                componentNames.put(component.getId(), component.getName());
            }
        } catch (PersistenceException e) {
            lblMessage.setText(e.getMessage());
        }
    }
}
