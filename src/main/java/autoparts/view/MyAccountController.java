package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.InvalidDataException;
import autoparts.exception.PersistenceException;
import autoparts.model.User;
import autoparts.service.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controlador de la pantalla Mi cuenta (MyAccount.fxml): muestra los datos del
 * usuario de la sesión, permite cambiar su nombre y su contraseña.
 *
 * Solo llama a UserService a través de AppServices.
 *
 * @author Jahdiel Torres
 */
public class MyAccountController {

    @FXML
    private TextField txtName;
    @FXML
    private Label lblEmail;
    @FXML
    private Label lblRole;
    @FXML
    private Label lblSince;
    @FXML
    private Label lblProfileMessage;
    @FXML
    private PasswordField txtCurrent;
    @FXML
    private PasswordField txtNew;
    @FXML
    private PasswordField txtConfirm;
    @FXML
    private Label lblPasswordMessage;

    /**
     * JavaFX llama este método al cargar la pantalla: muestra los datos del usuario.
     */
    @FXML
    private void initialize() {
        User user = Session.getUser();
        txtName.setText(user.getName());
        lblEmail.setText(user.getEmail());
        lblRole.setText(user.getRole());
        lblSince.setText(String.valueOf(user.getRegistrationDate()));
    }

    /**
     * Botón Guardar nombre.
     */
    @FXML
    private void saveName() {
        try {
            AppServices.users().updateName(Session.getUser(), txtName.getText());
            showSuccess(lblProfileMessage, "Nombre actualizado.");
        } catch (InvalidDataException | PersistenceException e) {
            showError(lblProfileMessage, e.getMessage());
        }
    }

    /**
     * Botón Cambiar contraseña (también con Enter en la confirmación).
     */
    @FXML
    private void changePassword() {
        try {
            AppServices.users().changePassword(Session.getUser(),
                    txtCurrent.getText(), txtNew.getText(), txtConfirm.getText());
            txtCurrent.clear();
            txtNew.clear();
            txtConfirm.clear();
            showSuccess(lblPasswordMessage, "Contraseña cambiada. Úsala la próxima vez que ingreses.");
        } catch (InvalidDataException | PersistenceException e) {
            showError(lblPasswordMessage, e.getMessage());
        }
    }

    private void showSuccess(Label label, String text) {
        label.getStyleClass().setAll("label", "success-message");
        label.setText(text);
    }

    private void showError(Label label, String text) {
        label.getStyleClass().setAll("label", "error-message");
        label.setText(text);
    }
}
