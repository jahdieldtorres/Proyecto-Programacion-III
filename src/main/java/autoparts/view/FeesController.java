package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.InvalidDataException;
import autoparts.exception.PersistenceException;
import autoparts.model.Fee;
import autoparts.model.Status;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Controlador de la pantalla de tarifas (Fees.fxml).
 *
 * Muestra en una tabla las tarifas de los servicios de los talleres: servicio,
 * valor en pesos, fecha desde la que rige y estado. La tarifa vigente de un
 * servicio es la que está ACTIVO. Permite buscar por servicio y filtrar por
 * estado.
 *
 * Por ahora el servicio se muestra como "Servicio #id", porque todavía no
 * existe el modelo ni el servicio de los servicios del taller (services.csv).
 * Cuando exista, solo hay que cambiar el método {@link #serviceName(int)}.
 *
 * Solo llama a los servicios a través de AppServices: nunca usa un DAO ni
 * FileUtil. Los errores se muestran en lblMessage.
 *
 * @author Héctor Guevara
 */
public class FeesController {

    /** Opción del filtro que muestra todos los estados. */
    private static final String ALL = "Todos";

    /** Formato en que se muestran los valores en pesos colombianos. */
    private static final NumberFormat PESOS = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));

    /** Formato en que se muestran las fechas en la tabla. */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private TableView<Fee> tblFees;
    @FXML
    private TableColumn<Fee, String> colService;
    @FXML
    private TableColumn<Fee, String> colAmount;
    @FXML
    private TableColumn<Fee, String> colStartDate;
    @FXML
    private TableColumn<Fee, String> colStatus;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cmbFilter;
    @FXML
    private Label lblMessage;

    /**
     * JavaFX llama este método al cargar la pantalla: configura las columnas y
     * el filtro, y muestra las tarifas.
     */
    @FXML
    private void initialize() {
        // Qué muestra cada columna: el valor de la fila, convertido a texto.
        colService.setCellValueFactory(row -> new SimpleStringProperty(serviceName(row.getValue().getServiceId())));
        colAmount.setCellValueFactory(row -> new SimpleStringProperty(PESOS.format(row.getValue().getAmount())));
        colStartDate.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getStartDate().format(DATE_FORMAT)));
        colStatus.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getStatus().name()));

        // Opciones del filtro por estado.
        cmbFilter.setItems(FXCollections.observableArrayList(ALL, Status.ACTIVO.name(), Status.INACTIVO.name()));
        cmbFilter.setValue(ALL);

        tblFees.setPlaceholder(new Label("No hay tarifas."));
        search();
    }

    /**
     * Se ejecuta con el botón Buscar, con Enter en el buscador o al cambiar el
     * filtro. Pide las tarifas al servicio y muestra las que cumplen la
     * búsqueda y el estado elegido.
     */
    @FXML
    private void search() {
        try {
            List<Fee> all = AppServices.fees().findAll();
            List<Fee> result = filter(all, txtSearch.getText(), cmbFilter.getValue());
            tblFees.setItems(FXCollections.observableArrayList(result));
            showInfo(result.size() + " resultado(s).");
        } catch (InvalidDataException e) {
            showError(e.getMessage());
        } catch (PersistenceException e) {
            showError("No se pudieron leer las tarifas. Intenta de nuevo.");
        }
    }

    /**
     * Devuelve el texto que identifica a un servicio en la tabla.
     *
     * @param serviceId identificador del servicio (en services.csv)
     * @return "Servicio #id"
     */
    private String serviceName(int serviceId) {
        return "Servicio #" + serviceId;
    }

    /**
     * Deja solo las tarifas cuyo servicio contiene el texto y que tienen el
     * estado elegido.
     *
     * @param items  tarifas a revisar
     * @param text   texto del buscador (puede estar vacío)
     * @param status estado elegido en el filtro, o "Todos"
     * @return lista con las tarifas que cumplen
     */
    private List<Fee> filter(List<Fee> items, String text, String status) {
        List<Fee> result = new ArrayList<>();
        String search = text == null ? "" : text.trim().toLowerCase();
        for (Fee item : items) {
            boolean matchesText = serviceName(item.getServiceId()).toLowerCase().contains(search);
            boolean matchesStatus = ALL.equals(status) || item.getStatus().name().equals(status);
            if (matchesText && matchesStatus) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Muestra un mensaje informativo (por ejemplo, la cantidad de resultados).
     *
     * @param text mensaje a mostrar
     */
    private void showInfo(String text) {
        lblMessage.getStyleClass().removeAll("error-message", "secondary-text");
        lblMessage.getStyleClass().add("secondary-text");
        lblMessage.setText(text);
    }

    /**
     * Muestra un mensaje de error en rojo y deja la tabla vacía.
     *
     * @param text mensaje de error
     */
    private void showError(String text) {
        lblMessage.getStyleClass().removeAll("error-message", "secondary-text");
        lblMessage.getStyleClass().add("error-message");
        lblMessage.setText(text);
        tblFees.setItems(FXCollections.observableArrayList());
    }
}