package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.InvalidDataException;
import autoparts.exception.PersistenceException;
import autoparts.model.Category;
import autoparts.model.Component;
import autoparts.service.Session;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

/**
 * Controlador de la pantalla del catálogo (Catalog.fxml).
 *
 * Muestra los componentes que devuelve ComponentService y permite filtrarlos
 * por categoría. El cliente solo ve los activos; el administrador ve también
 * los inactivos (columna Estado) y puede crear, editar y desactivar
 * componentes. Hecho a partir de ListTemplateController.
 *
 * @author Jahdiel Torres, ALVARO
 */
public class CatalogController {

    /** Opción "Todas" del filtro: su id 0 significa que no se filtra por categoría. */
    private static final Category ALL = new Category(0, "Todas");

    /** Texto que se escribió en el buscador de Inicio; se usa una sola vez al abrir. */
    private static String pendingSearch = "";

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
    private TableColumn<Component, String> colStatus;
    @FXML
    private Button btnNew;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnDeactivate;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<Category> cmbCategory;
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
        colStatus.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getStatus().name()));

        cmbCategory.setConverter(new StringConverter<>() {
            @Override
            public String toString(Category category) {
                return category == null ? "" : category.getName();
            }

            @Override
            public Category fromString(String text) {
                return null;
            }
        });

        tblComponents.setPlaceholder(new Label("No hay componentes que coincidan con la búsqueda."));
        showAdminButtons();
        loadCategories();
        txtSearch.setText(pendingSearch);
        pendingSearch = "";
        search();
    }

    /**
     * Guarda un texto para buscarlo apenas se abra el catálogo. Lo usa la
     * pantalla de Inicio antes de pedirle al Dashboard que muestre el catálogo.
     *
     * @param text lo que el usuario escribió en el buscador de Inicio
     */
    public static void setPendingSearch(String text) {
        pendingSearch = text == null ? "" : text.trim();
    }

    /**
     * Se ejecuta con el botón Buscar, con Enter en el buscador o al cambiar la categoría.
     */
    @FXML
    private void search() {
        lblMessage.getStyleClass().remove("error-message");
        try {
            List<Component> result = findComponents(txtSearch.getText(), selectedCategoryId());
            tblComponents.setItems(FXCollections.observableArrayList(result));
            lblMessage.setText(result.size() + " componente(s).");
        } catch (PersistenceException e) {
            lblMessage.getStyleClass().add("error-message");
            lblMessage.setText(e.getMessage());
        }
    }

    /**
     * Nuevo, Editar y Desactivar, y la columna Estado, solo los ve el administrador.
     */
    private void showAdminButtons() {
        boolean admin = Session.isAdmin();
        for (Button button : new Button[]{btnNew, btnEdit, btnDeactivate}) {
            button.setVisible(admin);
            button.setManaged(admin);
        }
        colStatus.setVisible(admin);
    }

    /**
     * El administrador ve todos los componentes; los demás solo los activos.
     * La búsqueda (texto sin tildes y categoría) la hace el servicio.
     */
    private List<Component> findComponents(String text, Integer categoryId) {
        return Session.isAdmin()
                ? AppServices.components().searchAll(text, categoryId)
                : AppServices.components().search(text, categoryId);
    }

    /**
     * Id de la categoría elegida en el filtro, o null si está en "Todas".
     */
    private Integer selectedCategoryId() {
        Category selected = cmbCategory.getValue();
        return selected == null || selected.getId() == ALL.getId() ? null : selected.getId();
    }

    /**
     * Abre el formulario vacío para crear un componente y, al cerrarse, recarga la tabla.
     */
    @FXML
    private void newComponent() {
        Navigator.openForm("ComponentForm.fxml", "Nuevo componente",
                (ComponentFormController form) -> form.setComponent(null));
        reload();
    }

    /**
     * Abre el formulario con el componente elegido en la tabla y, al cerrarse,
     * recarga la tabla.
     */
    @FXML
    private void editComponent() {
        Component selected = tblComponents.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selecciona un componente de la tabla.");
            return;
        }
        Navigator.openForm("ComponentForm.fxml", "Editar componente",
                (ComponentFormController form) -> form.setComponent(selected));
        reload();
    }

    /**
     * Desactiva el componente elegido (borrado lógico: pasa a INACTIVO) y recarga la tabla.
     */
    @FXML
    private void deactivateComponent() {
        Component selected = tblComponents.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selecciona un componente de la tabla.");
            return;
        }
        try {
            AppServices.components().deactivate(selected.getId());
        } catch (InvalidDataException | PersistenceException e) {
            showError(e.getMessage());
            return;
        }
        reload();
    }

    /**
     * Vuelve a llenar el filtro de categorías y la tabla (después de un formulario).
     */
    private void reload() {
        loadCategories();
        search();
    }

    private void showError(String message) {
        if (!lblMessage.getStyleClass().contains("error-message")) {
            lblMessage.getStyleClass().add("error-message");
        }
        lblMessage.setText(message);
    }

    /**
     * Llena el filtro con las categorías que tienen componentes activos.
     */
    private void loadCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(ALL);
        try {
            for (Component component : findComponents("", null)) {
                Category category = component.getCategory();
                boolean added = categories.stream().anyMatch(c -> c.getId() == category.getId());
                if (!added) {
                    categories.add(category);
                }
            }
        } catch (PersistenceException e) {
            lblMessage.setText(e.getMessage());
        }
        cmbCategory.setItems(FXCollections.observableArrayList(categories));
        cmbCategory.setValue(ALL);
    }
}