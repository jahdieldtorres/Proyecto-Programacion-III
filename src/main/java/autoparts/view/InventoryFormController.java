package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.InvalidDataException;
import autoparts.exception.PersistenceException;
import autoparts.model.Component;
import autoparts.model.Condition;
import autoparts.model.Inventory;
import autoparts.model.Status;
import autoparts.model.Supplier;
import autoparts.util.ImageStorage;
import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

/**
 * Controlador del formulario de inventario (InventoryForm.fxml): crea o edita
 * una oferta de un proveedor por un componente. Se abre con
 * {@code Navigator.openForm("InventoryForm.fxml", titulo, form -> form.setInventory(oferta))};
 * con {@code null} crea una oferta nueva.
 *
 * Solo llama a {@code InventoryService} a través de {@link AppServices}. Los
 * errores del usuario llegan como {@link InvalidDataException} y se muestran
 * en lblMessage sin cerrar el formulario. Al editar, el proveedor y el
 * componente no se pueden cambiar.
 *
 * @author Héctor Guevara
 */
public class InventoryFormController {

    @FXML
    private Label lblTitle;
    @FXML
    private ComboBox<Supplier> cmbSupplier;
    @FXML
    private ComboBox<Component> cmbComponent;
    @FXML
    private TextField txtPrice;
    @FXML
    private TextField txtStock;
    @FXML
    private TextField txtShipping;
    @FXML
    private TextField txtWarranty;
    @FXML
    private ComboBox<Condition> cmbCondition;
    @FXML
    private ComboBox<Status> cmbStatus;
    @FXML
    private TextArea txtDescription;
    @FXML
    private Label lblPhoto;
    @FXML
    private Label lblMessage;
    @FXML
    private Button btnSave;

    /** Oferta que se está editando; null si se está creando una nueva. */
    private Inventory original;

    /** Foto elegida con el botón "Elegir foto"; null si no se eligió ninguna. */
    private File selectedPhoto;

    /**
     * JavaFX llama este método al cargar el formulario: llena los combos y deja
     * el formulario listo para crear una oferta.
     */
    @FXML
    private void initialize() {
        cmbSupplier.setConverter(new StringConverter<>() {
            @Override
            public String toString(Supplier supplier) {
                return supplier == null ? "" : supplier.getCompanyName();
            }

            @Override
            public Supplier fromString(String text) {
                return null;
            }
        });
        cmbComponent.setConverter(new StringConverter<>() {
            @Override
            public String toString(Component component) {
                return component == null ? "" : component.getName();
            }

            @Override
            public Component fromString(String text) {
                return null;
            }
        });
        cmbCondition.getItems().setAll(Condition.values());
        cmbCondition.setValue(Condition.NUEVO);
        cmbStatus.getItems().setAll(Status.values());
        cmbStatus.setValue(Status.ACTIVO);

        try {
            // Para crear solo se ofrecen los proveedores activos
            cmbSupplier.getItems().setAll(AppServices.partners().findAllSuppliers().stream()
                    .filter(supplier -> supplier.getStatus() == Status.ACTIVO)
                    .toList());
            cmbComponent.getItems().setAll(AppServices.components().findAll());
        } catch (InvalidDataException | PersistenceException e) {
            lblMessage.setText(e.getMessage());
        }
    }

    /**
     * Prepara el formulario. Navigator.openForm lo llama antes de mostrarlo.
     *
     * @param item oferta a editar, o null para crear una nueva
     */
    public void setInventory(Inventory item) {
        this.original = item;
        if (item == null) {
            lblTitle.setText("Nueva oferta");
            return;
        }
        lblTitle.setText("Editar oferta");
        selectSupplier(item.getSupplierId());
        cmbComponent.getItems().stream()
                .filter(component -> component.getId() == item.getComponentId())
                .findFirst()
                .ifPresent(cmbComponent::setValue);
        txtPrice.setText(String.valueOf(item.getPrice()));
        txtStock.setText(String.valueOf(item.getStock()));
        txtShipping.setText(String.valueOf(item.getShippingCost()));
        txtWarranty.setText(String.valueOf(item.getWarrantyMonths()));
        cmbCondition.setValue(item.getCondition());
        cmbStatus.setValue(item.getStatus());
        txtDescription.setText(item.getDescription());
        showCurrentPhoto(item.getId());
        // Al editar, el proveedor y el componente no se pueden cambiar.
        cmbSupplier.setDisable(true);
        cmbComponent.setDisable(true);
    }

    /**
     * Se ejecuta con el botón Guardar: arma la oferta, la envía al servicio y
     * cierra el formulario. Si hay un error, lo muestra y deja el formulario abierto.
     */
    @FXML
    private void save() {
        lblMessage.setText("");
        try {
            Inventory item = buildInventory();
            if (original == null) {
                AppServices.inventory().register(item);
                // Ya quedó guardada: si la foto falla, un nuevo clic en Guardar edita esta oferta.
                original = item;
            } else {
                AppServices.inventory().update(item);
            }
            try {
                savePhoto(item.getId());
            } catch (PersistenceException e) {
                lblMessage.setText("La oferta se guardó, pero no se pudo guardar la foto. Elige la foto y guarda de nuevo.");
                return;
            }
            Navigator.closeForm(btnSave);
        } catch (InvalidDataException e) {
            lblMessage.setText(e.getMessage());
        } catch (PersistenceException e) {
            lblMessage.setText("No se pudo guardar la oferta. Intenta de nuevo.");
        }
    }

    /**
     * Se ejecuta con el botón "Elegir foto": abre el selector de archivos y
     * deja elegida la foto (.jpg o .png). Solo se copia al guardar la oferta.
     */
    @FXML
    private void choosePhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Elegir la foto de la oferta");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fotos (*.jpg, *.jpeg, *.png)", "*.jpg", "*.jpeg", "*.png"));
        File file = chooser.showOpenDialog(btnSave.getScene().getWindow());
        if (file == null) {
            return;
        }
        if (!ImageStorage.isSupported(file)) {
            lblMessage.setText("La foto debe ser un archivo .jpg o .png.");
            return;
        }
        lblMessage.setText("");
        selectedPhoto = file;
        lblPhoto.setText(file.getName());
    }

    /**
     * Se ejecuta con el botón Cancelar: cierra el formulario sin guardar.
     */
    @FXML
    private void cancel() {
        Navigator.closeForm(btnSave);
    }

    /**
     * Guarda la foto elegida (si hay una) como la foto principal de la oferta.
     *
     * @param inventoryId identificador de la oferta ya guardada
     */
    private void savePhoto(int inventoryId) {
        if (selectedPhoto == null) {
            return;
        }
        AppServices.inventoryImages().saveMainImage(inventoryId, selectedPhoto);
        selectedPhoto = null;
    }

    /**
     * Al editar, muestra que la oferta ya tiene foto (o que no tiene).
     */
    private void showCurrentPhoto(int inventoryId) {
        try {
            boolean hasPhoto = AppServices.inventoryImages().findMainImageUrl(inventoryId) != null;
            lblPhoto.setText(hasPhoto ? "Ya tiene foto (elige otra para cambiarla)" : "Sin foto");
        } catch (PersistenceException e) {
            lblPhoto.setText("Sin foto");
        }
    }

    /**
     * Crea el objeto con lo escrito en el formulario. Al crear usa id 0; al
     * editar conserva el id del original. Si falta el proveedor o el componente
     * envía 0 y el servicio lo rechaza con su mensaje.
     */
    private Inventory buildInventory() {
        int id = original == null ? 0 : original.getId();
        Supplier supplier = cmbSupplier.getValue();
        Component component = cmbComponent.getValue();
        return new Inventory(id,
                supplier == null ? 0 : supplier.getId(),
                component == null ? 0 : component.getId(),
                readNumber(txtPrice, "El precio"),
                readNumber(txtStock, "El stock"),
                readNumber(txtShipping, "El costo de envío"),
                readNumber(txtWarranty, "La garantía"),
                cmbCondition.getValue(),
                txtDescription.getText(),
                cmbStatus.getValue());
    }

    /**
     * Pone en el combo al proveedor de la oferta que se edita, aunque esté
     * inactivo y por eso no aparezca en la lista de proveedores activos.
     */
    private void selectSupplier(int supplierId) {
        try {
            AppServices.partners().findById(supplierId).ifPresent(partner -> {
                if (partner instanceof Supplier supplier) {
                    cmbSupplier.getItems().setAll(supplier);
                    cmbSupplier.setValue(supplier);
                }
            });
        } catch (InvalidDataException | PersistenceException e) {
            lblMessage.setText(e.getMessage());
        }
    }

    /**
     * Lee un número entero de un campo de texto.
     *
     * @param field campo del que se lee
     * @param label nombre del dato para el mensaje, por ejemplo "El precio"
     * @return el número escrito
     * @throws InvalidDataException si el campo está vacío o no es un número
     */
    private int readNumber(TextField field, String label) {
        String text = field.getText() == null ? "" : field.getText().trim();
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new InvalidDataException(label + " debe ser un número.");
        }
    }
}