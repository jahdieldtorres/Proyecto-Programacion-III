package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.InvalidDataException;
import autoparts.exception.PersistenceException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controlador de la pantalla de registro de clientes (Register.fxml).
 *
 * @author Jahdiel Torres
 */
public class RegisterController {

    @FXML
    private TextField txtName;
    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private PasswordField txtConfirmation;
    @FXML
    private Label lblMessage;
    @FXML
    private Label lblSuccess;

    @FXML
    private void register() {
        lblMessage.setText("");
        lblSuccess.setText("");
        try {
            AppServices.users().register(txtName.getText(), txtEmail.getText(),
                    txtPassword.getText(), txtConfirmation.getText());
            lblSuccess.setText("Cuenta creada. Ya puedes iniciar sesión.");
            txtPassword.clear();
            txtConfirmation.clear();
        } catch (InvalidDataException e) {
            lblMessage.setText(e.getMessage());
        } catch (PersistenceException e) {
            lblMessage.setText("No se pudo guardar la cuenta. Intenta de nuevo.");
        }
    }

    @FXML
    private void backToLogin() {
        Navigator.goTo(Navigator.LOGIN);
    }
}
