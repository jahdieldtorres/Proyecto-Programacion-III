package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.PersistenceException;
import autoparts.model.Component;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Controlador de la pantalla del catálogo (Catalog.fxml).
 *
 * Muestra los componentes activos que devuelve ComponentService.search y
 * permite filtrarlos por categoría. Hecho a partir de ListTemplateController.
 *
 * @author Jahdiel Torres
 */
public class CatalogController {

    private static final String ALL = "Todas";

    @FXML
    private TableView<Component> tblComponents;
    @FXML
    private TableColumn<Component, String> colName;
    @FXML
    private TableColumn<Component, String> colReference;
    @FXML
    private TableColumn<Component, String> colType;
    @FXML
    private TableColumn<Component, String> colBrand;
    @FXML
    private TableColumn<Component, String> colCategory;
    @FXML
    private TableColumn<Component, String> colOrigin;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cmbCategory;
    @FXML
    private Label lblMessage;

    /**
     * JavaFX llama este método al cargar la pantalla: configura la tabla,
     * llena el filtro de categorías y muestra todo el catálogo.
     */
    @FXML
    private void initialize() {
        colName.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getName()));
        colReference.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getReference()));
        colType.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getType().getFileValue()));
        colBrand.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getBrand().getName()));
        colCategory.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getCategory().getName()));
        colOrigin.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getOrigin()));

        tblComponents.setPlaceholder(new Label("No hay componentes que coincidan con la búsqueda."));
        loadCategories();
        search();
    }

    /**
     * Se ejecuta con el botón Buscar, con Enter en el buscador o al cambiar la categoría.
     */
    @FXML
    private void search() {
        lblMessage.getStyleClass().remove("error-message");
        try {
            List<Component> found = AppServices.components().search(txtSearch.getText());
            List<Component> result = filterByCategory(found, cmbCategory.getValue());
            tblComponents.setItems(FXCollections.observableArrayList(result));
            lblMessage.setText(result.size() + " componente(s).");
        } catch (PersistenceException e) {
            lblMessage.getStyleClass().add("error-message");
            lblMessage.setText(e.getMessage());
        }
    }

    /**
     * Llena el filtro con las categorías que tienen componentes activos.
     */
    private void loadCategories() {
        List<String> categories = new ArrayList<>();
        categories.add(ALL);
        try {
            for (Component component : AppServices.components().search("")) {
                String name = component.getCategory().getName();
                if (!categories.contains(name)) {
                    categories.add(name);
                }
            }
        } catch (PersistenceException e) {
            lblMessage.setText(e.getMessage());
        }
        cmbCategory.setItems(FXCollections.observableArrayList(categories));
        cmbCategory.setValue(ALL);
    }

    /**
     * Deja solo los componentes de la categoría elegida ("Todas" no filtra).
     */
    private List<Component> filterByCategory(List<Component> components, String category) {
        if (category == null || ALL.equals(category)) {
            return components;
        }
        List<Component> result = new ArrayList<>();
        for (Component component : components) {
            if (component.getCategory().getName().equals(category)) {
                result.add(component);
            }
        }
        return result;
    }
}
