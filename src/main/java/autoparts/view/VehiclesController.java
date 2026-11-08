package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.InvalidDataException;
import autoparts.exception.PersistenceException;
import autoparts.model.Component;
import autoparts.model.Vehicle;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Controlador de la pantalla de vehículos (Vehicles.fxml). Es solo de
 * consulta: muestra una tabla de vehículos y, al elegir uno, los repuestos
 * compatibles con él. Solo llama a {@code VehicleService} a través de
 * {@link AppServices}. Hecho a partir de ListTemplateController.
 *
 * @author ALVARO
 */
public class VehiclesController {

    @FXML
    private TableView<Vehicle> tblVehicles;
    @FXML
    private TableColumn<Vehicle, String> colType;
    @FXML
    private TableColumn<Vehicle, String> colBrand;
    @FXML
    private TableColumn<Vehicle, String> colModel;
    @FXML
    private TableColumn<Vehicle, String> colYear;
    @FXML
    private TableColumn<Vehicle, String> colDetail;
    @FXML
    private TableView<Component> tblComponents;
    @FXML
    private TableColumn<Component, String> colComponentName;
    @FXML
    private TableColumn<Component, String> colComponentReference;
    @FXML
    private TableColumn<Component, String> colComponentType;
    @FXML
    private TableColumn<Component, String> colComponentBrand;
    @FXML
    private TableColumn<Component, String> colComponentCategory;
    @FXML
    private TextField txtSearch;
    @FXML
    private Label lblCompatible;
    @FXML
    private Label lblMessage;

    /**
     * JavaFX llama este método al cargar la pantalla: configura las tablas,
     * muestra los vehículos y escucha cuál se elige.
     */
    @FXML
    private void initialize() {
        colType.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getType().getDisplayName()));
        colBrand.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getBrand().getName()));
        colModel.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getModelName()));
        colYear.setCellValueFactory(row -> new SimpleStringProperty(String.valueOf(row.getValue().getYearRelease())));
        colDetail.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getDetail()));

        colComponentName.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getName()));
        colComponentReference.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getReference()));
        colComponentType.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getType().getFileValue()));
        colComponentBrand.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getBrand().getName()));
        colComponentCategory.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getCategory().getName()));

        tblVehicles.setPlaceholder(new Label("No hay vehículos que coincidan con la búsqueda."));
        tblComponents.setPlaceholder(new Label("Elige un vehículo para ver sus repuestos."));
        tblVehicles.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldVehicle, vehicle) -> showCompatible(vehicle));
        search();
    }

    /**
     * Se ejecuta con el botón Buscar o con Enter en el buscador.
     */
    @FXML
    private void search() {
        lblMessage.getStyleClass().remove("error-message");
        try {
            List<Vehicle> found = AppServices.vehicles().search(txtSearch.getText());
            tblVehicles.setItems(FXCollections.observableArrayList(found));
            lblMessage.setText(found.size() + " vehículo(s).");
        } catch (InvalidDataException | PersistenceException e) {
            showError(e.getMessage());
        }
    }

    /**
     * Llena la tabla de abajo con los repuestos compatibles del vehículo elegido.
     *
     * @param vehicle vehículo elegido, o null si se limpió la selección
     */
    private void showCompatible(Vehicle vehicle) {
        if (vehicle == null) {
            tblComponents.getItems().clear();
            lblCompatible.setText("Repuestos compatibles");
            return;
        }
        try {
            List<Component> components = AppServices.vehicles().findCompatibleComponents(vehicle.getId());
            tblComponents.setItems(FXCollections.observableArrayList(components));
            lblCompatible.setText("Repuestos compatibles con " + vehicle.getBrand().getName()
                    + " " + vehicle.getModelName() + " (" + components.size() + ")");
            tblComponents.setPlaceholder(new Label("Este vehículo no tiene repuestos compatibles."));
        } catch (InvalidDataException | PersistenceException e) {
            tblComponents.getItems().clear();
            showError(e.getMessage());
        }
    }

    private void showError(String message) {
        if (!lblMessage.getStyleClass().contains("error-message")) {
            lblMessage.getStyleClass().add("error-message");
        }
        lblMessage.setText(message);
    }
}
