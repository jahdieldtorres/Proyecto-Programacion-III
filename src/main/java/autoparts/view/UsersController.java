package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.InvalidDataException;
import autoparts.exception.PersistenceException;
import autoparts.model.User;
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
 * Controlador de la pantalla de usuarios (Users.fxml), solo para el administrador:
 * buscar usuarios, cambiar su rol y activarlos o desactivarlos.
 *
 * Hecho a partir de ListTemplateController. Solo llama a UserService.
 *
 * @author Jahdiel Torres
 */
public class UsersController {

    private static final String ALL = "Todos";

    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cmbFilter;
    @FXML
    private TableView<User> tblUsers;
    @FXML
    private TableColumn<User, String> colName;
    @FXML
    private TableColumn<User, String> colEmail;
    @FXML
    private TableColumn<User, String> colRole;
    @FXML
    private TableColumn<User, String> colStatus;
    @FXML
    private TableColumn<User, String> colSince;
    @FXML
    private ComboBox<String> cmbRole;
    @FXML
    private Label lblMessage;

    /**
     * JavaFX llama este método al cargar la pantalla: configura la tabla y los combos.
     */
    @FXML
    private void initialize() {
        colName.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getName()));
        colEmail.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getEmail()));
        colRole.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getRole()));
        colStatus.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getStatus()));
        colSince.setCellValueFactory(row -> new SimpleStringProperty(
                String.valueOf(row.getValue().getRegistrationDate())));

        cmbFilter.getItems().setAll(ALL, User.ROLE_CLIENT, User.ROLE_PARTNER, User.ROLE_ADMIN);
        cmbFilter.setValue(ALL);
        cmbRole.getItems().setAll(User.ROLE_CLIENT, User.ROLE_PARTNER, User.ROLE_ADMIN);
        tblUsers.setPlaceholder(new Label("No hay usuarios que coincidan con la búsqueda."));
        search();
    }

    /**
     * Botón Buscar, Enter en el buscador o cambio del filtro de rol.
     */
    @FXML
    private void search() {
        try {
            String role = ALL.equals(cmbFilter.getValue()) ? null : cmbFilter.getValue();
            List<User> users = AppServices.users().search(txtSearch.getText(), role);
            tblUsers.setItems(FXCollections.observableArrayList(users));
            lblMessage.setText(users.size() + " usuario(s).");
        } catch (PersistenceException e) {
            lblMessage.setText(e.getMessage());
        }
    }

    /**
     * Botón Asignar rol: cambia el rol del usuario seleccionado.
     */
    @FXML
    private void assignRole() {
        User selected = selectedUser();
        if (selected == null) {
            return;
        }
        try {
            AppServices.users().changeRole(selected.getId(), cmbRole.getValue());
            search();
            lblMessage.setText("Rol de " + selected.getName() + " actualizado.");
        } catch (InvalidDataException | PersistenceException e) {
            lblMessage.setText(e.getMessage());
        }
    }

    /**
     * Botón Activar / desactivar: cambia el estado del usuario seleccionado.
     */
    @FXML
    private void toggleStatus() {
        User selected = selectedUser();
        if (selected == null) {
            return;
        }
        try {
            AppServices.users().setActive(selected.getId(), !selected.isActive());
            search();
            lblMessage.setText("Estado de " + selected.getName() + " actualizado.");
        } catch (InvalidDataException | PersistenceException e) {
            lblMessage.setText(e.getMessage());
        }
    }

    /**
     * Devuelve el usuario seleccionado en la tabla, o null con un aviso si no hay ninguno.
     */
    private User selectedUser() {
        User selected = tblUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblMessage.setText("Selecciona un usuario de la tabla.");
        }
        return selected;
    }
}
