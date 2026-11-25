package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.InvalidDataException;
import autoparts.exception.PersistenceException;
import autoparts.model.CatalogItem;
import autoparts.model.ComparisonCriterion;
import autoparts.model.Component;
import autoparts.model.Offer;
import autoparts.service.Session;
import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Controlador del detalle de un repuesto (ComponentDetail.fxml).
 *
 * Muestra los datos del repuesto y compara las ofertas de todos los
 * almacenes que lo tienen (usa ComparatorService). Cada oferta tiene su botón
 * para agregarla al carrito. Las ofertas se ordenan por el criterio que elige
 * el cliente, sin marcar a ningún almacén como "el mejor": todos son aliados.
 *
 * @author Jahdiel Torres
 */
public class ComponentDetailController {

    /** Repuesto que se va a mostrar. Lo deja listo el catálogo o el Inicio antes de abrir la pantalla. */
    private static CatalogItem pendingItem;

    @FXML
    private ImageView imgPhoto;
    @FXML
    private Label lblNoPhoto;
    @FXML
    private Label lblBrand;
    @FXML
    private Label lblName;
    @FXML
    private Label lblReference;
    @FXML
    private Label lblCompatibility;
    @FXML
    private Label lblDescription;
    @FXML
    private Label lblCategory;
    @FXML
    private Label lblOrigin;
    @FXML
    private Label lblSummary;
    @FXML
    private Label lblMessage;
    @FXML
    private ComboBox<ComparisonCriterion> cmbCriterion;
    @FXML
    private VBox offersBox;

    private CatalogItem item;

    public static void setItem(CatalogItem selected) {
        pendingItem = selected;
    }

    /**
     * JavaFX llama este método automáticamente al cargar la pantalla.
     */
    @FXML
    private void initialize() {
        item = pendingItem;
        cmbCriterion.getItems().setAll(ComparisonCriterion.values());
        cmbCriterion.setValue(ComparisonCriterion.PRICE);
        if (item == null) {
            lblName.setText("Elige un repuesto en el catálogo.");
            return;
        }
        showComponent();
        loadOffers();
    }

    /**
     * Llena la columna izquierda con los datos del repuesto.
     */
    private void showComponent() {
        Component component = item.getComponent();
        lblBrand.setText(component.getBrand().getName().toUpperCase());
        lblName.setText(component.getName());
        lblReference.setText(component.getReference());
        lblCategory.setText("Categoría: " + component.getCategory().getName());
        lblOrigin.setText("Procedencia: " + component.getOrigin());

        // La descripción es opcional: si el repuesto no tiene, se oculta el texto.
        String description = component.getDescription();
        boolean hasDescription = description != null && !description.isBlank();
        lblDescription.setText(hasDescription ? description : "");
        lblDescription.setVisible(hasDescription);
        lblDescription.setManaged(hasDescription);

        if (item.getImageUrl() != null) {
            imgPhoto.setImage(new Image(item.getImageUrl(), true));
            lblNoPhoto.setVisible(false);
        }

        if (item.getCompatible() == null) {
            lblCompatibility.setVisible(false);
            lblCompatibility.setManaged(false);
        } else if (item.isCompatible()) {
            lblCompatibility.setText("✓ Compatible con tu " + Session.getVehicle().getModelName());
            lblCompatibility.getStyleClass().add("badge-blue");
        } else {
            lblCompatibility.setText("Compatibilidad por confirmar");
            lblCompatibility.getStyleClass().add("badge-gray");
        }
    }

    /**
     * Pide las ofertas ordenadas por el criterio elegido y crea una fila por cada una.
     */
    @FXML
    private void loadOffers() {
        if (item == null) {
            return;
        }
        offersBox.getChildren().clear();
        try {
            List<Offer> offers = AppServices.comparator().compare(item.getComponent().getId(), cmbCriterion.getValue());
            for (Offer offer : offers) {
                offersBox.getChildren().add(offerRow(offer));
            }
            lblSummary.setText(offers.isEmpty()
                    ? "Ningún almacén tiene este repuesto con stock por ahora."
                    : Formats.stores(offers.size()) + " lo tienen. Ordenadas por "
                    + cmbCriterion.getValue().getLabel().toLowerCase() + ".");
        } catch (InvalidDataException | PersistenceException e) {
            showMessage(e.getMessage(), true);
        }
    }

    /**
     * Fila de una oferta: almacén y condiciones a la izquierda; precio, cantidad
     * y botón para agregar al carrito a la derecha.
     */
    private HBox offerRow(Offer offer) {
        Label supplier = new Label(offer.getSupplierName());
        supplier.getStyleClass().add("offer-supplier");
        HBox title = new HBox(8, supplier);
        title.setAlignment(Pos.CENTER_LEFT);
        Label details = new Label(Formats.condition(offer.getCondition())
                + " · Garantía " + offer.getWarrantyMonths() + " meses"
                + " · Entrega en " + offer.getDeliveryDays() + " días"
                + " · " + offer.getStock() + " disponibles");
        details.getStyleClass().add("secondary-text");
        VBox left = new VBox(4, title, details);
        String description = offer.getDescription();
        if (description != null && !description.isBlank()) {
            Label descriptionLabel = new Label(description);
            descriptionLabel.setWrapText(true);
            descriptionLabel.setMaxWidth(420);
            left.getChildren().add(descriptionLabel);
        }

        Label price = new Label(Formats.money(offer.getPrice()));
        price.getStyleClass().add("product-price");
        Label shipping = new Label("+ envío " + Formats.money(offer.getShippingCost()));
        shipping.getStyleClass().add("secondary-text");
        VBox priceBox = new VBox(2, price, shipping);
        priceBox.setAlignment(Pos.CENTER_RIGHT);

        Spinner<Integer> quantity = new Spinner<>(1, offer.getStock(), 1);
        quantity.setPrefWidth(80);
        Button add = new Button("Agregar");
        add.getStyleClass().add("action-button");
        add.setOnAction(event -> addToCart(offer, quantity.getValue()));

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox row = new HBox(16, offerPhoto(offer), left, space, priceBox, quantity, add);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().addAll("card", "offer-row");
        return row;
    }

    /**
     * Foto pequeña de la oferta. Si la tienda no subió foto, se usa la del
     * repuesto; si tampoco hay, queda el texto "Sin foto".
     */
    private StackPane offerPhoto(Offer offer) {
        String url = null;
        try {
            url = AppServices.inventoryImages().findMainImageUrl(offer.getItem().getId());
        } catch (PersistenceException e) {
            // Si falla la consulta de la foto, se usa la del repuesto.
        }
        if (url == null) {
            url = item.getImageUrl();
        }
        StackPane box = new StackPane();
        box.setMinSize(84, 84);
        box.setPrefSize(84, 84);
        box.setMaxSize(84, 84);
        box.getStyleClass().add("product-photo");
        if (url == null) {
            Label noPhoto = new Label("Sin foto");
            noPhoto.getStyleClass().add("secondary-text");
            box.getChildren().add(noPhoto);
        } else {
            ImageView view = new ImageView(new Image(url, 84, 84, true, true, true));
            view.setFitWidth(84);
            view.setFitHeight(84);
            view.setPreserveRatio(true);
            box.getChildren().add(view);
        }
        return box;
    }

    /**
     * Agrega la oferta al carrito de la sesión y actualiza el número de la barra.
     */
    private void addToCart(Offer offer, int quantity) {
        Session.getCart().add(offer, quantity);
        DashboardController.refreshCartCount();
        showMessage("Agregado al carrito: " + quantity + " × " + item.getComponent().getName()
                + " de " + offer.getSupplierName() + ".", false);
    }

    private void showMessage(String text, boolean error) {
        lblMessage.getStyleClass().removeAll("error-message", "success-message");
        lblMessage.getStyleClass().add(error ? "error-message" : "success-message");
        lblMessage.setText(text);
    }

    @FXML
    private void back() {
        DashboardController.openCatalogWith("");
    }
}