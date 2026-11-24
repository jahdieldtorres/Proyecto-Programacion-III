package autoparts.view;

import autoparts.exception.InvalidDataException;
import autoparts.exception.PersistenceException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Plantilla de controlador para los formularios (FormTemplate.fxml).
 *
 * Se abre desde una pantalla de lista con
 * {@code Navigator.openForm("MiForm.fxml", "Editar ...", (MiFormController f) -> f.setItem(objeto));}
 * Con null se crea un registro nuevo. Los errores llegan como
 * InvalidDataException y se muestran en lblMessage sin cerrar el formulario.
 *
 * Los comentarios REEMPLAZAR indican lo que cambia en cada formulario.
 *
 * @author Jahdiel Torres
 */
public class FormTemplateController {

    @FXML
    private Label lblTitle;
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtQuantity;
    @FXML
    private ComboBox<String> cmbStatus;
    @FXML
    private Label lblMessage;
    @FXML
    private Button btnSave;

    // REEMPLAZAR: el tipo del objeto que edita el formulario (Component, Inventory...)
    private Object original;

    /**
     * JavaFX llama este método al cargar el formulario: llena los combos.
     */
    @FXML
    private void initialize() {
        cmbStatus.getItems().setAll("ACTIVO", "INACTIVO");
        cmbStatus.setValue("ACTIVO");
    }

    /**
     * Prepara el formulario. Navigator.openForm lo llama antes de mostrarlo.
     *
     * @param item objeto a editar, o null para crear uno nuevo
     */
    public void setItem(Object item) {
        this.original = item;
        lblTitle.setText(item == null ? "Nuevo registro" : "Editar registro");
        // REEMPLAZAR: si item no es null, llenar los campos con sus datos
    }

    /**
     * Botón Guardar: arma el objeto, lo envía al servicio y cierra el formulario.
     */
    @FXML
    private void save() {
        lblMessage.setText("");
        try {
            int quantity = readNumber(txtQuantity, "La cantidad");
            // REEMPLAZAR: crear o actualizar el objeto con el servicio, por ejemplo:
            // if (original == null) AppServices.inventory().register(item);
            // else AppServices.inventory().update(item);
            Navigator.closeForm(btnSave);
        } catch (InvalidDataException | PersistenceException e) {
            lblMessage.setText(e.getMessage());
        }
    }

    /**
     * Botón Cancelar: cierra el formulario sin guardar.
     */
    @FXML
    private void cancel() {
        Navigator.closeForm(btnSave);
    }

    /**
     * Lee un número entero de un campo de texto.
     *
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
