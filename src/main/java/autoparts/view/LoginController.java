package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.InvalidDataException;
import autoparts.exception.PersistenceException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controlador de la pantalla de inicio de sesión (Login.fxml).
 *
 * Envía el correo y la contraseña a UserService y muestra el mensaje de error
 * si las credenciales no son válidas.
 *
 * @author Jahdiel Torres
 */
public class LoginController {

    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label lblMessage;

    @FXML
    private void login() {
        try {
            AppServices.users().login(txtEmail.getText(), txtPassword.getText());
            Navigator.goTo(Navigator.DASHBOARD);
        } catch (InvalidDataException e) {
            lblMessage.setText(e.getMessage());
        } catch (PersistenceException e) {
            lblMessage.setText("No se pudieron leer los usuarios. Intenta de nuevo.");
        }
    }

    @FXML
    private void createAccount() {
        Navigator.goTo(Navigator.REGISTER);
    }
}
