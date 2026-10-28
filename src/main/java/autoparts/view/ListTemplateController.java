package autoparts.view;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * PLANTILLA del controlador de una pantalla de listado (ListTemplate.fxml).
 *
 * Para crear su pantalla: copien esta clase con el nombre de su pantalla
 * (CatalogController, PartnersController...) y cambien lo que dice REEMPLAZAR.
 * Usa datos de ejemplo fijos para que funcione sola; en su copia, los datos
 * vienen del servicio a través de AppServices.
 *
 * @author Jahdiel Torres
 */
public class ListTemplateController {

    private static final String ALL = "Todos";
    private static final NumberFormat PESOS = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));

    // REEMPLAZAR: ExampleItem por su clase del modelo (Component, Partner, Offer...)
    @FXML
    private TableView<ExampleItem> tblItems;
    @FXML
    private TableColumn<ExampleItem, String> colName;
    @FXML
    private TableColumn<ExampleItem, String> colDetail;
    @FXML
    private TableColumn<ExampleItem, String> colPrice;
    @FXML
    private TableColumn<ExampleItem, String> colStatus;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cmbFilter;
    @FXML
    private Label lblMessage;

    /**
     * JavaFX llama este método al cargar la pantalla: configura la tabla y
     * muestra los datos iniciales.
     */
    @FXML
    private void initialize() {
        // Qué muestra cada columna: el valor de la fila, convertido a texto.
        // REEMPLAZAR: los getters por los de su clase.
        colName.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getName()));
        colDetail.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getDetail()));
        colPrice.setCellValueFactory(row -> new SimpleStringProperty(PESOS.format(row.getValue().getPrice())));
        colStatus.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getStatus()));

        // Opciones del filtro. REEMPLAZAR: en el comparador, aquí van los criterios.
        cmbFilter.setItems(FXCollections.observableArrayList(ALL, "ACTIVO", "INACTIVO"));
        cmbFilter.setValue(ALL);

        tblItems.setPlaceholder(new Label("No hay resultados."));
        highlightFirstRow();
        search();
    }

    /**
     * Se ejecuta con el botón Buscar, con Enter en el buscador o al cambiar el filtro.
     */
    @FXML
    private void search() {
        lblMessage.setText("");
        // REEMPLAZAR: llamar a su servicio, por ejemplo
        // List<Component> items = AppServices.components().search(txtSearch.getText());
        // y capturar InvalidDataException y PersistenceException para mostrar el mensaje en lblMessage.
        List<ExampleItem> items = filter(exampleData(), txtSearch.getText(), cmbFilter.getValue());
        tblItems.setItems(FXCollections.observableArrayList(items));
        lblMessage.setText(items.size() + " resultado(s).");
    }

    /**
     * Resalta la primera fila con la clase highlight-row de styles.css.
     * El comparador la usa para marcar la mejor oferta; si su pantalla no
     * lo necesita, borren este método y su llamada en initialize().
     */
    private void highlightFirstRow() {
        tblItems.setRowFactory(table -> new TableRow<>() {
            @Override
            protected void updateItem(ExampleItem item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("highlight-row");
                if (!empty && getIndex() == 0) {
                    getStyleClass().add("highlight-row");
                }
            }
        });
    }

    // ---------------- Solo para la plantilla: borrar en su copia ----------------

    private List<ExampleItem> filter(List<ExampleItem> items, String text, String status) {
        List<ExampleItem> result = new ArrayList<>();
        String search = text == null ? "" : text.trim().toLowerCase();
        for (ExampleItem item : items) {
            boolean matchesText = item.getName().toLowerCase().contains(search);
            boolean matchesStatus = ALL.equals(status) || item.getStatus().equals(status);
            if (matchesText && matchesStatus) {
                result.add(item);
            }
        }
        return result;
    }

    private List<ExampleItem> exampleData() {
        return List.of(
                new ExampleItem("Pastillas de freno delanteras", "AutoMax Repuestos", 172000, "ACTIVO"),
                new ExampleItem("Filtro de aceite", "Repuestos del Caribe", 28500, "ACTIVO"),
                new ExampleItem("Batería 12V 60Ah", "Importadora Cesar", 395000, "ACTIVO"),
                new ExampleItem("Amortiguador delantero", "MotoPartes Valledupar", 210000, "INACTIVO"));
    }

    /**
     * Fila de ejemplo de la plantilla. En su copia se usa la clase del modelo.
     */
    public static class ExampleItem {

        private final String name;
        private final String detail;
        private final double price;
        private final String status;

        public ExampleItem(String name, String detail, double price, String status) {
            this.name = name;
            this.detail = detail;
            this.price = price;
            this.status = status;
        }

        public String getName() {
            return name;
        }

        public String getDetail() {
            return detail;
        }

        public double getPrice() {
            return price;
        }

        public String getStatus() {
            return status;
        }
    }
}
