package autoparts.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controlador de la pantalla de inicio (Home.fxml).
 *
 * PENDIENTE: los contadores se llenarán con los servicios de catálogo y socios,
 * y la búsqueda llevará al comparador.
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

    @FXML
    private void initialize() {
        lblParts.setText("0");
        lblStores.setText("0");
        lblWorkshops.setText("0");
    }

    @FXML
    private void search() {
        if (txtSearch.getText().isBlank()) {
            lblMessage.setText("Escribe el nombre o la referencia de un repuesto.");
            return;
        }
        lblMessage.setText("La búsqueda estará disponible con el comparador.");
    }
}
