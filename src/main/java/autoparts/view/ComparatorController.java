package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.InvalidDataException;
import autoparts.exception.PersistenceException;
import autoparts.model.Component;
import autoparts.model.ComparisonCriterion;
import autoparts.model.Offer;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

/**
 * Controlador de la pantalla del comparador (Comparator.fxml): el usuario busca
 * un componente, elige un criterio y ve las ofertas de los proveedores con la
 * mejor resaltada en la primera fila. Llama a {@code ComponentService.search}
 * y {@code ComparatorService.compare} a través de {@link AppServices}.
 *
 * @author Héctor Guevara
 */
public class ComparatorController {

    private static final NumberFormat MONEY = NumberFormat.getIntegerInstance(Locale.of("es", "CO"));

    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<Component> cmbComponent;
    @FXML
    private ComboBox<ComparisonCriterion> cmbCriterion;
    @FXML
    private TableView<Offer> tblOffers;
    @FXML
    private TableColumn<Offer, String> colSupplier;
    @FXML
    private TableColumn<Offer, String> colPrice;
    @FXML
    private TableColumn<Offer, String> colShipping;
    @FXML
    private TableColumn<Offer, String> colTotal;
    @FXML
    private TableColumn<Offer, String> colWarranty;
    @FXML
    private TableColumn<Offer, String> colDelivery;
    @FXML
    private TableColumn<Offer, String> colCondition;
    @FXML
    private TableColumn<Offer, String> colStock;
    @FXML
    private Label lblSummary;
    @FXML
    private Label lblMessage;

    /**
     * JavaFX llama este método automáticamente al cargar la pantalla.
     */
    @FXML
    private void initialize() {
        colSupplier.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSupplierName()));
        colPrice.setCellValueFactory(c -> new SimpleStringProperty(money(c.getValue().getPrice())));
        colShipping.setCellValueFactory(c -> new SimpleStringProperty(money(c.getValue().getShippingCost())));
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(money(c.getValue().getTotalCost())));
        colWarranty.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWarrantyMonths() + " meses"));
        colDelivery.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDeliveryDays() + " días"));
        colCondition.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCondition()));
        colStock.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getStock())));

        // El orden lo define el criterio elegido, no el clic en una columna
        for (TableColumn<Offer, ?> column : tblOffers.getColumns()) {
            column.setSortable(false);
        }
        tblOffers.setPlaceholder(new Label("Elige un componente y un criterio, y pulsa Comparar."));
        tblOffers.setRowFactory(table -> new TableRow<>() {
            @Override
            protected void updateItem(Offer offer, boolean empty) {
                super.updateItem(offer, empty);
                getStyleClass().remove("highlight-row");
                if (!empty && offer != null && offer == table.getItems().get(0)) {
                    getStyleClass().add("highlight-row");
                }
            }
        });

        cmbComponent.setConverter(new StringConverter<>() {
            @Override
            public String toString(Component component) {
                return component == null ? "" : component.getName();
            }

            @Override
            public Component fromString(String text) {
                return null;
            }
        });
        cmbCriterion.getItems().setAll(ComparisonCriterion.values());
        cmbCriterion.setValue(ComparisonCriterion.PRICE);

        search();
    }

    /**
     * Busca componentes por nombre o referencia y los pone en la lista para elegir.
     * Con el campo vacío trae todos los componentes activos.
     */
    @FXML
    private void search() {
        clearMessages();
        try {
            List<Component> found = AppServices.components().search(txtSearch.getText().trim());
            cmbComponent.getItems().setAll(found);
            if (found.isEmpty()) {
                lblMessage.setText("No se encontraron componentes con ese texto.");
            } else {
                cmbComponent.setValue(found.get(0));
            }
        } catch (InvalidDataException | PersistenceException e) {
            lblMessage.setText(e.getMessage());
        }
    }

    /**
     * Compara las ofertas del componente elegido con el criterio elegido y
     * muestra la mejor opción.
     */
    @FXML
    private void compare() {
        clearMessages();
        Component component = cmbComponent.getValue();
        if (component == null) {
            lblMessage.setText("Busca y selecciona un componente.");
            return;
        }
        try {
            ComparisonCriterion criterion = cmbCriterion.getValue();
            List<Offer> offers = AppServices.comparator().compare(component.getId(), criterion);
            tblOffers.getItems().setAll(offers);
            if (offers.isEmpty()) {
                lblSummary.setText("Ningún proveedor tiene stock de este componente.");
            } else {
                Offer best = offers.get(0);
                lblSummary.setText("Mejor opción (" + criterion.getLabel().toLowerCase() + "): "
                        + best.getSupplierName() + ", " + money(best.getTotalCost()) + " con envío.");
            }
        } catch (InvalidDataException | PersistenceException e) {
            tblOffers.getItems().clear();
            lblMessage.setText(e.getMessage());
        }
    }

    private void clearMessages() {
        lblMessage.setText("");
        lblSummary.setText("");
    }

    private String money(int pesos) {
        return "$ " + MONEY.format(pesos);
    }
}