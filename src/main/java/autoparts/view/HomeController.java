package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.PersistenceException;
import autoparts.model.Partner;
import autoparts.model.Status;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controlador de la pantalla de inicio (Home.fxml): muestra los contadores
 * reales del sistema y lleva la búsqueda al catálogo.
 *
 * @author Jahdiel Torres
 */
public class HomeController {

    @FXML
    private TextField txtSearch;
    @FXML
    private Label lblParts;
    @FXML
    private Label lblStores;
    @FXML
    private Label lblWorkshops;
    @FXML
    private Label lblMessage;

    /**
     * JavaFX llama este método al cargar la pantalla: cuenta los repuestos
     * activos, los proveedores activos y los talleres activos.
     */
    @FXML
    private void initialize() {
        try {
            lblParts.setText(String.valueOf(AppServices.components().search("").size()));
            lblStores.setText(String.valueOf(countActive(AppServices.partners().findAllSuppliers())));
            lblWorkshops.setText(String.valueOf(countActive(AppServices.partners().findAllWorkshops())));
        } catch (PersistenceException e) {
            lblParts.setText("-");
            lblStores.setText("-");
            lblWorkshops.setText("-");
            lblMessage.setText("No se pudieron leer los datos: " + e.getMessage());
        }
    }

    /**
     * Se ejecuta con el botón Buscar o con Enter: abre el catálogo con lo escrito.
     */
    @FXML
    private void search() {
        if (txtSearch.getText().isBlank()) {
            lblMessage.setText("Escribe el nombre o la referencia de un repuesto.");
            return;
        }
        DashboardController.openCatalogWith(txtSearch.getText());
    }

    /**
     * Cuenta los socios que están activos. Recibe proveedores o talleres porque
     * ambos son Partner (sustitución de Liskov).
     */
    private int countActive(List<? extends Partner> partners) {
        int count = 0;
        for (Partner partner : partners) {
            if (partner.getStatus() == Status.ACTIVO) {
                count++;
            }
        }
        return count;
    }
}
