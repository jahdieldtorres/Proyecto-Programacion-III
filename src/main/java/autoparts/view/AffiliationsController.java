package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.InvalidDataException;
import autoparts.exception.PersistenceException;
import autoparts.model.Affiliation;
import autoparts.model.Partner;
import autoparts.model.Status;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Controlador de la pantalla de afiliaciones (Affiliations.fxml).
 *
 * Muestra en una tabla qué proveedores están afiliados a qué talleres, con la
 * fecha de inicio y el estado. Permite buscar por nombre del proveedor o del
 * taller y filtrar por estado (ACTIVO / INACTIVO).
 *
 * Solo llama a los servicios a través de AppServices: nunca usa un DAO ni
 * FileUtil. Los errores se muestran en lblMessage.
 *
 * @author Héctor Guevara
 */
public class AffiliationsController {

    /** Opción del filtro que muestra todos los estados. */
    private static final String ALL = "Todos";

    /** Formato en que se muestran las fechas en la tabla. */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private TableView<Affiliation> tblAffiliations;
    @FXML
    private TableColumn<Affiliation, String> colSupplier;
    @FXML
    private TableColumn<Affiliation, String> colWorkshop;
    @FXML
    private TableColumn<Affiliation, String> colStartDate;
    @FXML
    private TableColumn<Affiliation, String> colStatus;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cmbFilter;
    @FXML
    private Label lblMessage;

    /**
     * Nombres de los socios por identificador. La afiliación solo guarda los
     * ids del proveedor y del taller; con este mapa se muestra su nombre.
     */
    private final Map<Integer, String> partnerNames = new HashMap<>();

    /**
     * JavaFX llama este método al cargar la pantalla: configura las columnas y
     * el filtro, y muestra las afiliaciones.
     */
    @FXML
    private void initialize() {
        // Qué muestra cada columna: el valor de la fila, convertido a texto.
        colSupplier.setCellValueFactory(row -> new SimpleStringProperty(partnerName(row.getValue().getSupplierId())));
        colWorkshop.setCellValueFactory(row -> new SimpleStringProperty(partnerName(row.getValue().getWorkshopId())));
        colStartDate.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getStartDate().format(DATE_FORMAT)));
        colStatus.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getStatus().name()));

        // Opciones del filtro por estado.
        cmbFilter.setItems(FXCollections.observableArrayList(ALL, Status.ACTIVO.name(), Status.INACTIVO.name()));
        cmbFilter.setValue(ALL);

        tblAffiliations.setPlaceholder(new Label("No hay afiliaciones."));
        search();
    }

    /**
     * Se ejecuta con el botón Buscar, con Enter en el buscador o al cambiar el
     * filtro. Pide las afiliaciones al servicio y muestra las que cumplen la
     * búsqueda y el estado elegido.
     */
    @FXML
    private void search() {
        try {
            loadPartnerNames();
            List<Affiliation> all = AppServices.affiliations().findAll();
            List<Affiliation> result = filter(all, txtSearch.getText(), cmbFilter.getValue());
            tblAffiliations.setItems(FXCollections.observableArrayList(result));
            showInfo(result.size() + " resultado(s).");
        } catch (InvalidDataException e) {
            showError(e.getMessage());
        } catch (PersistenceException e) {
            showError("No se pudieron leer las afiliaciones. Intenta de nuevo.");
        }
    }

    /**
     * Carga el nombre de todos los socios en {@link #partnerNames}.
     */
    private void loadPartnerNames() {
        partnerNames.clear();
g        for (Partner partner : AppServices.partners().findAll()) {
            partnerNames.put(partner.getId(), partner.getCompanyName());
        }
    }

    /**
     * Devuelve el nombre de un socio. Si ya no existe, muestra su número.
     *
     * @param id identificador del socio
     * @return nombre de la compañía, o "Socio #id" si no se encuentra
     */
    private String partnerName(int id) {
        return partnerNames.getOrDefault(id, "Socio #" + id);
    }

    /**
     * Deja solo las afiliaciones cuyo proveedor o taller contiene el texto y
     * que tienen el estado elegido.
     *
     * @param items  afiliaciones a revisar
     * @param text   texto del buscador (puede estar vacío)
     * @param status estado elegido en el filtro, o "Todos"
     * @return lista con las afiliaciones que cumplen
     */
    private List<Affiliation> filter(List<Affiliation> items, String text, String status) {
        List<Affiliation> result = new ArrayList<>();
        String search = text == null ? "" : text.trim().toLowerCase();
        for (Affiliation item : items) {
            String supplier = partnerName(item.getSupplierId()).toLowerCase();
            String workshop = partnerName(item.getWorkshopId()).toLowerCase();
            boolean matchesText = supplier.contains(search) || workshop.contains(search);
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
        tblAffiliations.setItems(FXCollections.observableArrayList());
    }
}