package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.PersistenceException;
import autoparts.model.CatalogItem;
import autoparts.model.Category;
import autoparts.model.Vehicle;
import autoparts.service.Session;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/**
 * Controlador del catálogo del cliente (Shop.fxml): muestra los repuestos
 * como tarjetas y deja filtrarlos por categoría y por compatibilidad con el
 * vehículo elegido. Al hacer clic en una tarjeta abre el detalle del repuesto.
 *
 * Toda la información la pide a CatalogService en una sola llamada.
 *
 * @author Jahdiel Torres
 */
public class ShopController {

    /** Texto que llega del buscador de la barra superior. */
    private static String pendingSearch = "";
    /** Categoría que llega desde Inicio (null = todas). */
    private static Integer pendingCategoryId;

    @FXML
    private VBox vehicleBox;
    @FXML
    private Label lblVehicle;
    @FXML
    private FlowPane categoryChips;
    @FXML
    private CheckBox chkCompatible;
    @FXML
    private Label lblNoVehicle;
    @FXML
    private Label lblTitle;
    @FXML
    private Label lblCount;
    @FXML
    private Button btnClearSearch;
    @FXML
    private ScrollPane scroll;
    @FXML
    private FlowPane cards;
    @FXML
    private VBox emptyBox;

    private final ToggleGroup categoryGroup = new ToggleGroup();
    private String searchText;

    /**
     * Lo que se pide antes de abrir el catálogo: un texto buscado, una categoría o ninguno.
     */
    public static void setPendingSearch(String text) {
        pendingSearch = text == null ? "" : text.trim();
    }

    public static void setPendingCategory(Integer categoryId) {
        pendingCategoryId = categoryId;
    }

    /**
     * JavaFX llama este método automáticamente al cargar la pantalla.
     */
    @FXML
    private void initialize() {
        searchText = pendingSearch;
        pendingSearch = "";
        showVehicle();
        loadCategories(pendingCategoryId);
        pendingCategoryId = null;
        search();
    }

    /**
     * Muestra el vehículo elegido. Si no hay, oculta la tarjeta y desactiva el filtro.
     */
    private void showVehicle() {
        Vehicle vehicle = Session.getVehicle();
        boolean hasVehicle = vehicle != null;
        vehicleBox.setVisible(hasVehicle);
        vehicleBox.setManaged(hasVehicle);
        chkCompatible.setDisable(!hasVehicle);
        chkCompatible.setSelected(hasVehicle);
        lblNoVehicle.setVisible(!hasVehicle);
        lblNoVehicle.setManaged(!hasVehicle);
        if (hasVehicle) {
            lblVehicle.setText(vehicleName(vehicle));
        }
    }

    /**
     * Crea un botón por categoría, más "Todas". Solo uno puede estar marcado.
     */
    private void loadCategories(Integer selectedId) {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category(0, "Todas"));
        try {
            categories.addAll(AppServices.components().findCategories());
        } catch (PersistenceException e) {
            lblCount.setText(e.getMessage());
        }
        for (Category category : categories) {
            ToggleButton chip = new ToggleButton(category.getName());
            chip.getStyleClass().add("chip-toggle");
            chip.setUserData(category.getId());
            chip.setToggleGroup(categoryGroup);
            boolean selected = selectedId == null ? category.getId() == 0 : category.getId() == selectedId;
            chip.setSelected(selected);
            chip.setOnAction(event -> {
                chip.setSelected(true); // no se puede desmarcar la categoría actual
                search();
            });
            categoryChips.getChildren().add(chip);
        }
    }

    /**
     * Pide los repuestos con los filtros elegidos y crea una tarjeta por cada uno.
     */
    @FXML
    private void search() {
        try {
            List<CatalogItem> items = AppServices.catalog().search(searchText, selectedCategoryId(), vehicleId());
            if (chkCompatible.isSelected()) {
                items.removeIf(item -> !item.isCompatible());
            }
            showCards(items);
        } catch (PersistenceException e) {
            cards.getChildren().clear();
            lblCount.setText(e.getMessage());
        }
    }

    private void showCards(List<CatalogItem> items) {
        cards.getChildren().clear();
        for (CatalogItem item : items) {
            cards.getChildren().add(new ComponentCard(item, () -> DashboardController.openDetail(item)));
        }
        boolean empty = items.isEmpty();
        emptyBox.setVisible(empty);
        scroll.setVisible(!empty);

        lblTitle.setText(searchText.isEmpty() ? selectedCategoryName() : "Resultados para \"" + searchText + "\"");
        lblCount.setText(items.size() + (items.size() == 1 ? " repuesto" : " repuestos")
                + " · ordenados por menor precio");
        btnClearSearch.setVisible(!searchText.isEmpty());
        btnClearSearch.setManaged(!searchText.isEmpty());
    }

    @FXML
    private void clearSearch() {
        searchText = "";
        search();
    }

    @FXML
    private void clearVehicle() {
        Session.setVehicle(null);
        showVehicle();
        search();
    }

    /**
     * Quita la búsqueda, vuelve a "Todas" y desmarca "solo compatibles".
     */
    @FXML
    private void clearAll() {
        searchText = "";
        chkCompatible.setSelected(false);
        categoryGroup.getToggles().get(0).setSelected(true);
        search();
    }

    private Integer selectedCategoryId() {
        int id = (int) categoryGroup.getSelectedToggle().getUserData();
        return id == 0 ? null : id;
    }

    private String selectedCategoryName() {
        String name = ((ToggleButton) categoryGroup.getSelectedToggle()).getText();
        return "Todas".equals(name) ? "Catálogo" : name;
    }

    private Integer vehicleId() {
        Vehicle vehicle = Session.getVehicle();
        return vehicle == null ? null : vehicle.getId();
    }

    /**
     * Nombre corto del vehículo, por ejemplo "Chevrolet Spark GT · 2018".
     */
    static String vehicleName(Vehicle vehicle) {
        return vehicle.getBrand().getName() + " " + vehicle.getModelName() + " · " + vehicle.getYearRelease();
    }
}
