package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.InvalidDataException;
import autoparts.exception.PersistenceException;
import autoparts.model.Partner;
import autoparts.model.Status;
import autoparts.model.Supplier;
import autoparts.model.Workshop;
import java.time.LocalDate;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

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
            } else {
                AppServices.partners().update(partner);
            }
            Navigator.closeForm(btnSave);
        } catch (InvalidDataException e) {
            lblMessage.setText(e.getMessage());
        } catch (PersistenceException e) {
            lblMessage.setText("No se pudo guardar el socio. Intenta de nuevo.");
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
     * Crea el objeto según el tipo elegido. Al crear usa id 0, estado ACTIVO y
     * la fecha de hoy; al editar conserva el id, el estado y la fecha del original.
     */
    private Partner buildPartner() {
        int id = original == null ? 0 : original.getId();
        Status status = original == null ? Status.ACTIVO : original.getStatus();
        LocalDate date = original == null ? LocalDate.now() : original.getRegistrationDate();
        String name = txtName.getText();
        String taxId = txtTaxId.getText();
        String phone = txtPhone.getText();

        return switch (cmbType.getValue()) {
            case SUPPLIER -> new Supplier(id, name, taxId, phone, status, date,
                    readNumber(txtDeliveryDays, "Los días de entrega"));
            default -> new Workshop(id, name, taxId, phone, status, date,
                    txtSpecialty.getText(), readNumber(txtBayCount, "Las bahías"));
        };
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
}
