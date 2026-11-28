package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.InvalidDataException;
import autoparts.exception.PersistenceException;
import autoparts.model.Partner;
import autoparts.model.Status;
import autoparts.model.Supplier;
import autoparts.model.Workshop;
import autoparts.util.ImageStorage;
import java.io.File;
import java.time.LocalDate;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

/**
 * Controlador del formulario de socios (PartnerForm.fxml): crea o edita un
 * proveedor o un taller. Se abre con
 * {@code Navigator.openForm("PartnerForm.fxml", titulo, form -> form.setPartner(socio))};
 * con {@code null} crea un socio nuevo.
 *
 * Solo llama a {@code PartnerService} a través de {@link AppServices}. Los
 * errores del usuario llegan como {@link InvalidDataException} y se muestran
 * en lblMessage sin cerrar el formulario. Al editar, el tipo no se puede cambiar.
 *
 * @author Héctor Guevara
 */
public class PartnerFormController {

    /** Tipos de socio que se pueden elegir al crear. */
    private enum PartnerType {
        SUPPLIER("Proveedor"),
        WORKSHOP("Taller");

        private final String label;

        PartnerType(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    @FXML
    private Label lblTitle;
    @FXML
    private ComboBox<PartnerType> cmbType;
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtTaxId;
    @FXML
    private TextField txtPhone;
    @FXML
    private TextField txtAddress;
    @FXML
    private TextField txtLatitude;
    @FXML
    private TextField txtLongitude;
    @FXML
    private TextArea txtDescription;
    @FXML
    private Label lblPhoto;
    @FXML
    private VBox boxSupplier;
    @FXML
    private TextField txtDeliveryDays;
    @FXML
    private VBox boxWorkshop;
    @FXML
    private TextField txtSpecialty;
    @FXML
    private TextField txtBayCount;
    @FXML
    private Label lblMessage;
    @FXML
    private Button btnSave;

    /** Socio que se está editando; null si se está creando uno nuevo. */
    private Partner original;

    /** Foto elegida con el botón "Elegir foto"; null si no se eligió ninguna. */
    private File selectedPhoto;

    /**
     * JavaFX llama este método al cargar el formulario: llena el selector de
     * tipo y deja el formulario listo para crear un socio.
     */
    @FXML
    private void initialize() {
        cmbType.getItems().setAll(PartnerType.values());
        cmbType.setValue(PartnerType.SUPPLIER);
        showTypeFields();
    }

    /**
     * Prepara el formulario. Navigator.openForm lo llama antes de mostrarlo.
     *
     * @param partner socio a editar, o null para crear uno nuevo
     */
    public void setPartner(Partner partner) {
        this.original = partner;
        if (partner == null) {
            lblTitle.setText("Nuevo socio");
            return;
        }
        lblTitle.setText("Editar socio");
        txtName.setText(partner.getCompanyName());
        txtTaxId.setText(partner.getTaxId());
        txtPhone.setText(partner.getPhone());
        txtAddress.setText(partner.getAddress());
        txtLatitude.setText(String.valueOf(partner.getLatitude()));
        txtLongitude.setText(String.valueOf(partner.getLongitude()));
        txtDescription.setText(partner.getDescription());
        lblPhoto.setText(partner.getImageFile() == null
                ? "Sin foto" : "Ya tiene foto (elige otra para cambiarla)");
        if (partner instanceof Supplier supplier) {
            cmbType.setValue(PartnerType.SUPPLIER);
            txtDeliveryDays.setText(String.valueOf(supplier.getDeliveryDays()));
        } else if (partner instanceof Workshop workshop) {
            cmbType.setValue(PartnerType.WORKSHOP);
            txtSpecialty.setText(workshop.getSpecialty());
            txtBayCount.setText(String.valueOf(workshop.getBayCount()));
        }
        // Al editar, el tipo no se puede cambiar.
        cmbType.setDisable(true);
        showTypeFields();
    }

    /**
     * Se ejecuta al cambiar el tipo: muestra los campos del tipo elegido.
     */
    @FXML
    private void onTypeChanged() {
        showTypeFields();
    }

    /**
     * Se ejecuta con el botón Guardar: arma el socio, lo envía al servicio y
     * cierra el formulario. Si hay un error, lo muestra y deja el formulario abierto.
     */
    @FXML
    private void save() {
        lblMessage.setText("");
        try {
            Partner partner = buildPartner();
            if (original == null) {
                AppServices.partners().register(partner);
                // Ya quedó guardado: si la foto falla, un nuevo clic en Guardar lo edita.
                original = partner;
            } else {
                AppServices.partners().update(partner);
            }
            try {
                savePhoto(partner.getId());
            } catch (PersistenceException e) {
                lblMessage.setText("El socio se guardó, pero no se pudo guardar la foto. Elige la foto y guarda de nuevo.");
                return;
            }
            Navigator.closeForm(btnSave);
        } catch (InvalidDataException e) {
            lblMessage.setText(e.getMessage());
        } catch (PersistenceException e) {
            lblMessage.setText("No se pudo guardar el socio. Intenta de nuevo.");
        }
    }

    /**
     * Se ejecuta con el botón "Elegir foto": abre el selector de archivos y
     * deja elegida la foto (.jpg o .png). Solo se copia al guardar el socio.
     */
    @FXML
    private void choosePhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Elegir la foto del socio");
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
     * Guarda la foto elegida (si hay una) en el socio ya guardado.
     *
     * @param partnerId identificador del socio ya guardado
     */
    private void savePhoto(int partnerId) {
        if (selectedPhoto == null) {
            return;
        }
        AppServices.partners().changePhoto(partnerId, selectedPhoto);
        selectedPhoto = null;
    }

    /**
     * Se ejecuta con el botón Cancelar: cierra el formulario sin guardar.
     */
    @FXML
    private void cancel() {
        Navigator.closeForm(btnSave);
    }

    /**
     * Crea el objeto según el tipo elegido. Al crear usa id 0, estado ACTIVO y
     * la fecha de hoy; al editar conserva el id, el estado, la fecha y la foto del original.
     */
    private Partner buildPartner() {
        int id = original == null ? 0 : original.getId();
        Status status = original == null ? Status.ACTIVO : original.getStatus();
        LocalDate date = original == null ? LocalDate.now() : original.getRegistrationDate();
        String name = txtName.getText();
        String taxId = txtTaxId.getText();
        String phone = txtPhone.getText();
        String address = txtAddress.getText();
        double latitude = readDecimal(txtLatitude, "La latitud");
        double longitude = readDecimal(txtLongitude, "La longitud");

        Partner partner = switch (cmbType.getValue()) {
            case SUPPLIER -> new Supplier(id, name, taxId, phone, status, date,
                    address, latitude, longitude,
                    readNumber(txtDeliveryDays, "Los días de entrega"));
            default -> new Workshop(id, name, taxId, phone, status, date,
                    address, latitude, longitude,
                    txtSpecialty.getText(), readNumber(txtBayCount, "Las bahías"));
        };
        partner.setDescription(txtDescription.getText());
        // La foto actual se conserva; si se eligió otra, la cambia savePhoto() después de guardar.
        partner.setImageFile(original == null ? null : original.getImageFile());
        return partner;
    }

    /**
     * Muestra solo los campos del tipo elegido (proveedor o taller).
     */
    private void showTypeFields() {
        boolean supplier = cmbType.getValue() == PartnerType.SUPPLIER;
        boxSupplier.setVisible(supplier);
        boxSupplier.setManaged(supplier);
        boxWorkshop.setVisible(!supplier);
        boxWorkshop.setManaged(!supplier);
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

    /**
     * Lee un número decimal de un campo de texto. Acepta coma o punto como
     * separador decimal (por ejemplo 10,46 o 10.46).
     *
     * @param field campo del que se lee
     * @param label nombre del dato para el mensaje, por ejemplo "La latitud"
     * @return el número escrito
     * @throws InvalidDataException si el campo está vacío o no es un número
     */
    private double readDecimal(TextField field, String label) {
        String text = field.getText() == null ? "" : field.getText().trim().replace(',', '.');
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new InvalidDataException(label + " debe ser un número, por ejemplo 10.46.");
        }
    }
}