package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.InvalidDataException;
import autoparts.exception.PersistenceException;
import autoparts.model.Brand;
import autoparts.model.Category;
import autoparts.model.Component;
import autoparts.model.ComponentType;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controlador del formulario de componentes (ComponentForm.fxml): crea o edita
 * un componente. Se abre con
 * {@code Navigator.openForm("ComponentForm.fxml", titulo, form -> form.setComponent(componente))};
 * con {@code null} crea un componente nuevo.
 *
 * Solo llama a {@code ComponentService} a través de {@link AppServices}. Los
 * errores del usuario llegan como {@link InvalidDataException} y se muestran
 * en lblMessage sin cerrar el formulario. Al editar se cambian los datos del
 * objeto original con sus setters, así una llanta sigue siendo una llanta
 * (polimorfismo) y sus medidas no se pierden.
 *
 * @author ALVARO
 */
public class ComponentFormController {

    private static final String ORIGINAL = "ORIGINAL";
    private static final String GENERIC = "GENERICO";

    @FXML
    private Label lblTitle;
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtReference;
    @FXML
    private ComboBox<Brand> cmbBrand;
    @FXML
    private ComboBox<Category> cmbCategory;
    @FXML
    private ComboBox<String> cmbOrigin;
    @FXML
    private Label lblMessage;
    @FXML
    private Button btnSave;

    /** Componente que se está editando; null si se está creando uno nuevo. */
    private Component original;

    /**
     * JavaFX llama este método al cargar el formulario: llena los selectores
     * de marca, categoría y origen.
     */
    @FXML
    private void initialize() {
        cmbOrigin.getItems().setAll(ORIGINAL, GENERIC);
        cmbOrigin.setValue(GENERIC);
        try {
            cmbBrand.getItems().setAll(AppServices.components().findBrands());
            cmbCategory.getItems().setAll(AppServices.components().findCategories());
        } catch (PersistenceException e) {
            lblMessage.setText(e.getMessage());
        }
    }

    /**
     * Prepara el formulario. Navigator.openForm lo llama antes de mostrarlo.
     *
     * @param component componente a editar, o null para crear uno nuevo
     */
    public void setComponent(Component component) {
        this.original = component;
        if (component == null) {
            lblTitle.setText("Nuevo componente");
            return;
        }
        lblTitle.setText("Editar componente");
        txtName.setText(component.getName());
        txtReference.setText(component.getReference());
        cmbBrand.setValue(findBrand(component.getBrand()));
        cmbCategory.setValue(findCategory(component.getCategory()));
        cmbOrigin.setValue(component.getOrigin());
    }

    /**
     * Se ejecuta con el botón Guardar: arma el componente, lo envía al
     * servicio y cierra el formulario. Si hay un error, lo muestra y deja el
     * formulario abierto.
     */
    @FXML
    private void save() {
        lblMessage.setText("");
        try {
            if (original == null) {
                AppServices.components().register(new Component(ComponentType.GENERAL, 0,
                        txtName.getText(), txtReference.getText(), cmbBrand.getValue(),
                        cmbCategory.getValue(), cmbOrigin.getValue(), "ACTIVO"));
            } else {
                original.setName(txtName.getText());
                original.setReference(txtReference.getText());
                original.setBrand(cmbBrand.getValue());
                original.setCategory(cmbCategory.getValue());
                original.setOrigin(cmbOrigin.getValue());
                AppServices.components().update(original);
            }
            Navigator.closeForm(btnSave);
        } catch (InvalidDataException e) {
            lblMessage.setText(e.getMessage());
        } catch (PersistenceException e) {
            lblMessage.setText("No se pudo guardar el componente. Intenta de nuevo.");
        }
    }

    /**
     * Se ejecuta con el botón Cancelar: cierra el formulario sin guardar.
     */
    @FXML
    private void cancel() {
        Navigator.closeForm(btnSave);
    }

    /**
     * Busca en el selector la marca con el mismo id, para que quede
     * seleccionada aunque sea otra instancia del mismo registro.
     */
    private Brand findBrand(Brand brand) {
        for (Brand item : cmbBrand.getItems()) {
            if (brand != null && item.getId() == brand.getId()) {
                return item;
            }
        }
        return null;
    }

    /**
     * Busca en el selector la categoría con el mismo id.
     */
    private Category findCategory(Category category) {
        for (Category item : cmbCategory.getItems()) {
            if (category != null && item.getId() == category.getId()) {
                return item;
            }
        }
        return null;
    }
}
