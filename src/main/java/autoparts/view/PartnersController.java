package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.InvalidDataException;
import autoparts.exception.PersistenceException;
import autoparts.model.Partner;
import autoparts.model.Supplier;
import autoparts.model.User;
import autoparts.model.Workshop;
import autoparts.service.Session;

import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Controlador de la pantalla de socios (Partners.fxml): muestra en una tabla
 * los proveedores y talleres, con un filtro por tipo. El administrador además
 * puede crear, editar y desactivar socios (los botones se ocultan a los demás
 * roles). Solo llama a {@code PartnerService} a través de {@link AppServices}.
 *
 * @author Héctor Guevara
 */
public class PartnersController {

    private static final String ALL = "Todos";
    private static final String SUPPLIERS = "Proveedores";
    private static final String WORKSHOPS = "Talleres";

    @FXML
    private ComboBox<String> cmbFilter;
    @FXML
    private TableView<Partner> tblPartners;
    @FXML
    private TableColumn<Partner, String> colName;
    @FXML
    private TableColumn<Partner, String> colType;
    @FXML
    private TableColumn<Partner, String> colTaxId;
    @FXML
    private TableColumn<Partner, String> colPhone;
    @FXML
    private TableColumn<Partner, String> colDetail;
    @FXML
    private TableColumn<Partner, String> colStatus;
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

    /**
     * JavaFX llama este método automáticamente al cargar la pantalla.
     */
    @FXML
    private void initialize() {
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCompanyName()));
        colType.setCellValueFactory(c -> new SimpleStringProperty(typeName(c.getValue())));
        colTaxId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTaxId()));
        colPhone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhone()));
        colDetail.setCellValueFactory(c -> new SimpleStringProperty(detail(c.getValue())));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));
        tblPartners.setPlaceholder(new Label("No hay socios para mostrar."));

        cmbFilter.getItems().setAll(ALL, SUPPLIERS, WORKSHOPS);
        cmbFilter.setValue(ALL);
        showAdminButtons();
        load();
    }

    /**
     * Nuevo, Editar y Desactivar solo los ve el administrador.
     */
    private void showAdminButtons() {
        User user = Session.getUser();
        boolean admin = user != null && User.ROLE_ADMIN.equals(user.getRole());
        for (Button button : new Button[]{btnNew, btnEdit, btnDeactivate}) {
            button.setVisible(admin);
            button.setManaged(admin);
        }
    }

    /**
     * Abre el formulario vacío para crear un socio y, al cerrarse, recarga la tabla.
     */
    @FXML
    private void newPartner() {
        Navigator.openForm("PartnerForm.fxml", "Nuevo socio",
                (PartnerFormController form) -> form.setPartner(null));
        load();
    }

    /**
     * Abre el formulario con el socio elegido en la tabla y, al cerrarse,
     * recarga la tabla.
     */
    @FXML
    private void editPartner() {
        Partner selected = tblPartners.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblMessage.setText("Selecciona un socio de la tabla.");
            return;
        }
        Navigator.openForm("PartnerForm.fxml", "Editar socio",
                (PartnerFormController form) -> form.setPartner(selected));
        load();
    }

    /**
     * Desactiva el socio elegido (borrado lógico: pasa a INACTIVO) y recarga la tabla.
     */
    @FXML
    private void deactivatePartner() {
        Partner selected = tblPartners.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblMessage.setText("Selecciona un socio de la tabla.");
            return;
        }
        try {
            AppServices.partners().delete(selected.getId());
        } catch (InvalidDataException | PersistenceException e) {
            lblMessage.setText(e.getMessage());
            return;
        }
        load();
    }

    /**
     * Carga la tabla según el tipo elegido en el filtro. También se ejecuta
     * cuando cambia el filtro.
     */
    @FXML
    private void load() {
        lblMessage.setText("");
        try {
            List<? extends Partner> partners;
            partners = switch (cmbFilter.getValue()) {
                case SUPPLIERS -> AppServices.partners().findAllSuppliers();
                case WORKSHOPS -> AppServices.partners().findAllWorkshops();
                default -> AppServices.partners().findAll();
            };
            tblPartners.getItems().setAll(partners);
            lblCount.setText(partners.size() == 1 ? "1 socio" : partners.size() + " socios");
        } catch (InvalidDataException | PersistenceException e) {
            tblPartners.getItems().clear();
            lblCount.setText("");
            lblMessage.setText(e.getMessage());
        }
    }

    private String typeName(Partner partner) {
        return partner instanceof Supplier ? "Proveedor" : "Taller";
    }

    private String detail(Partner partner) {
        if (partner instanceof Supplier supplier) {
            return "Entrega en " + supplier.getDeliveryDays() + " días";
        }
        Workshop workshop = (Workshop) partner;
        return workshop.getSpecialty() + " · " + workshop.getBayCount() + " bahías";
    }
}