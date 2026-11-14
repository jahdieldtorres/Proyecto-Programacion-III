package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.InvalidDataException;
import autoparts.exception.PersistenceException;
import autoparts.model.Component;
import autoparts.model.Inventory;
import autoparts.model.Partner;
import autoparts.model.Supplier;
import autoparts.service.Session;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Controlador de la pantalla de inventario (Inventory.fxml): muestra en una
 * tabla las ofertas de los proveedores (componente, precio, stock, envío,
 * garantía, condición y estado), con un filtro por proveedor y un buscador por
 * componente. Solo el administrador entra a esta pantalla: puede crear, editar
 * y desactivar ofertas. Solo llama a {@code InventoryService} a través de
 * {@link AppServices}.
 *
 * @author Héctor Guevara
 */
public class InventoryController {

    private static final String ALL = "Todos";
    private static final NumberFormat MONEY = NumberFormat.getIntegerInstance(Locale.of("es", "CO"));

    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cmbSupplier;
    @FXML
    private TableView<Inventory> tblInventory;
    @FXML
    private TableColumn<Inventory, String> colSupplier;
    @FXML
    private TableColumn<Inventory, String> colComponent;
    @FXML
    private TableColumn<Inventory, String> colPrice;
    @FXML
    private TableColumn<Inventory, String> colStock;
    @FXML
    private TableColumn<Inventory, String> colShipping;
    @FXML
    private TableColumn<Inventory, String> colWarranty;
    @FXML
    private TableColumn<Inventory, String> colCondition;
    @FXML
    private TableColumn<Inventory, String> colStatus;
    @FXML
    private Button btnNew;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnDeactivate;
    @FXML
    private Label lblCount;
    @FXML
    private Label lblMessage;

    /** Proveedores del filtro, en el mismo orden que las opciones del combo (después de "Todos"). */
    private List<Supplier> suppliers = new ArrayList<>();
    /** Nombre de cada socio y de cada componente por su id, para mostrarlos en la tabla. */
    private final Map<Integer, String> supplierNames = new HashMap<>();
    private final Map<Integer, String> componentNames = new HashMap<>();

    /**
     * JavaFX llama este método automáticamente al cargar la pantalla.
     */
    @FXML
    private void initialize() {
        colSupplier.setCellValueFactory(c -> new SimpleStringProperty(supplierName(c.getValue().getSupplierId())));
        colComponent.setCellValueFactory(c -> new SimpleStringProperty(componentName(c.getValue().getComponentId())));
        colPrice.setCellValueFactory(c -> new SimpleStringProperty(money(c.getValue().getPrice())));
        colStock.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getStock())));
        colShipping.setCellValueFactory(c -> new SimpleStringProperty(money(c.getValue().getShippingCost())));
        colWarranty.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWarrantyMonths() + " meses"));
        colCondition.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCondition().name()));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));
        tblInventory.setPlaceholder(new Label("No hay ofertas para mostrar."));

        showAdminButtons();
        if (!Session.isAdmin()) {
            lblMessage.setText("Solo el administrador puede gestionar el inventario.");
            return;
        }
        loadSuppliers();
        load();
    }

    /**
     * Nuevo, Editar y Desactivar solo los ve el administrador.
     */
    private void showAdminButtons() {
        boolean admin = Session.isAdmin();
        for (Button button : new Button[]{btnNew, btnEdit, btnDeactivate}) {
            button.setVisible(admin);
            button.setManaged(admin);
        }
    }

    /**
     * Llena el filtro con "Todos" y los proveedores.
     */
    private void loadSuppliers() {
        try {
            suppliers = new ArrayList<>(AppServices.partners().findAllSuppliers());
        } catch (InvalidDataException | PersistenceException e) {
            lblMessage.setText(e.getMessage());
            return;
        }
        List<String> options = new ArrayList<>();
        options.add(ALL);
        for (Supplier supplier : suppliers) {
            options.add(supplier.getCompanyName());
        }
        cmbSupplier.getItems().setAll(options);
        cmbSupplier.getSelectionModel().select(0);
    }

    /**
     * Abre el formulario vacío para crear una oferta y, al cerrarse, recarga la tabla.
     */
    @FXML
    private void newItem() {
        Navigator.openForm("InventoryForm.fxml", "Nueva oferta",
                (InventoryFormController form) -> form.setInventory(null));
        load();
    }

    /**
     * Abre el formulario con la oferta elegida en la tabla y, al cerrarse,
     * recarga la tabla.
     */
    @FXML
    private void editItem() {
        Inventory selected = tblInventory.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblMessage.setText("Selecciona una oferta de la tabla.");
            return;
        }
        Navigator.openForm("InventoryForm.fxml", "Editar oferta",
                (InventoryFormController form) -> form.setInventory(selected));
        load();
    }

    /**
     * Desactiva la oferta elegida (borrado lógico: pasa a INACTIVO) y recarga la tabla.
     */
    @FXML
    private void deactivateItem() {
        Inventory selected = tblInventory.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblMessage.setText("Selecciona una oferta de la tabla.");
            return;
        }
        try {
            AppServices.inventory().delete(selected.getId());
        } catch (InvalidDataException e) {
            lblMessage.setText(e.getMessage());
            return;
        } catch (PersistenceException e) {
            lblMessage.setText("No se pudo desactivar la oferta. Intenta de nuevo.");
            return;
        }
        load();
    }

    /**
     * Carga la tabla según el proveedor elegido y el texto buscado. También se
     * ejecuta con el botón Buscar, con Enter en el buscador y al cambiar el filtro.
     */
    @FXML
    private void load() {
        if (!Session.isAdmin()) {
            return;
        }
        lblMessage.setText("");
        try {
            loadNames();
            int index = cmbSupplier.getSelectionModel().getSelectedIndex();
            List<Inventory> items;
            if (index > 0 && index <= suppliers.size()) {
                items = AppServices.inventory().findBySupplier(suppliers.get(index - 1).getId());
            } else {
                items = AppServices.inventory().findAll();
            }
            items = filterByComponent(items, txtSearch.getText());
            tblInventory.getItems().setAll(items);
            lblCount.setText(items.size() == 1 ? "1 oferta" : items.size() + " ofertas");
        } catch (InvalidDataException e) {
            showError(e.getMessage());
        } catch (PersistenceException e) {
            showError("No se pudo leer el inventario. Intenta de nuevo.");
        }
    }

    /**
     * Deja solo las ofertas cuyo componente contiene el texto buscado.
     */
    private List<Inventory> filterByComponent(List<Inventory> items, String text) {
        String search = text == null ? "" : text.trim().toLowerCase();
        List<Inventory> result = new ArrayList<>();
        for (Inventory item : items) {
            if (componentName(item.getComponentId()).toLowerCase().contains(search)) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Pide los socios y los componentes para poder mostrar sus nombres en la tabla.
     */
    private void loadNames() {
        supplierNames.clear();
        for (Partner partner : AppServices.partners().findAll()) {
            supplierNames.put(partner.getId(), partner.getCompanyName());
        }
        componentNames.clear();
        for (Component component : AppServices.components().findAll()) {
            componentNames.put(component.getId(), component.getName());
        }
    }

    private String supplierName(int supplierId) {
        return supplierNames.getOrDefault(supplierId, "Proveedor #" + supplierId);
    }

    private String componentName(int componentId) {
        return componentNames.getOrDefault(componentId, "Componente #" + componentId);
    }

    private String money(int pesos) {
        return "$ " + MONEY.format(pesos);
    }

    private void showError(String text) {
        tblInventory.getItems().clear();
        lblCount.setText("");
        lblMessage.setText(text);
    }
}