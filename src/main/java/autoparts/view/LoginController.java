package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.InvalidDataException;
import autoparts.exception.PersistenceException;
import autoparts.service.Session;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Controlador de la pantalla de inicio de sesión (Login.fxml).
 *
 * Envía el correo y la contraseña a UserService. Si los datos no son válidos
 * muestra el error y sacude el formulario. También deja mostrar u ocultar la
 * contraseña. El logo animado de la izquierda es el componente LogoTree.
 *
 * El botón "Explorar como invitado" entra a la app sin cuenta, solo para mirar.
 *
 * @author Jahdiel Torres
 * @author Héctor Guevara (modo invitado)
 */
public class LoginController {

    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private TextField txtPasswordVisible;
    @FXML
    private Button btnShowPassword;
    @FXML
    private Label lblMessage;
    @FXML
    private VBox formBox;

    /**
     * JavaFX llama este método automáticamente al cargar la pantalla.
     */
    @FXML
    private void initialize() {
        // Los dos campos de contraseña comparten el mismo texto
        txtPasswordVisible.textProperty().bindBidirectional(txtPassword.textProperty());
    }

    @FXML
    private void login() {
        try {
            AppServices.users().login(txtEmail.getText(), txtPassword.getText());
            Navigator.goTo(Navigator.DASHBOARD);
        } catch (InvalidDataException e) {
            showError(e.getMessage());
        } catch (PersistenceException e) {
            showError("No se pudieron leer los usuarios. Revisa tu conexión e intenta de nuevo.");
        }
    }

    /**
     * Cambia entre ver la contraseña escrita y ocultarla con puntos.
     */
    @FXML
    private void togglePassword() {
        boolean show = !txtPasswordVisible.isVisible();
        txtPasswordVisible.setVisible(show);
        txtPassword.setVisible(!show);
        btnShowPassword.setText(show ? "Ocultar" : "Mostrar");
    }

    /**
     * Botón "Explorar como invitado": abre la app sin cuenta. La sesión de
     * invitado vive en memoria; no se crea nada en Oracle.
     */
    @FXML
    private void exploreAsGuest() {
        Session.startGuest();
        AppServices.locations().forgetGuestLocation();
        Navigator.goTo(Navigator.DASHBOARD);
    }

    @FXML
    private void createAccount() {
        Navigator.goTo(Navigator.REGISTER);
    }

    /**
     * Muestra el mensaje de error y sacude el formulario de lado a lado.
     */
    private void showError(String message) {
        lblMessage.setText(message);
        TranslateTransition shake = new TranslateTransition(Duration.millis(60), formBox);
        shake.setFromX(0);
        shake.setByX(8);
        shake.setAutoReverse(true);
        shake.setCycleCount(6);
        shake.setOnFinished(event -> formBox.setTranslateX(0));
        shake.play();
    }
}
