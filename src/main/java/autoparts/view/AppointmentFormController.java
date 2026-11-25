package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.InvalidDataException;
import autoparts.exception.PersistenceException;
import autoparts.model.WorkshopService;
import autoparts.service.AppointmentService;
import autoparts.service.Session;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DateCell;
import javafx.scene.control.Label;

/**
 * Controlador del formulario para agendar una cita (AppointmentForm.fxml).
 *
 * Se abre desde Servicios con Navigator.openForm y recibe el servicio que el
 * cliente eligió con {@link #setup}. Las reglas (horario, anticipación,
 * citas repetidas) las valida AppointmentService; aquí solo se muestra el
 * mensaje si algo falla.
 *
 * @author Héctor Guevara
 */
public class AppointmentFormController {

    @FXML
    private Label lblService;
    @FXML
    private Label lblWorkshop;
    @FXML
    private DatePicker dpDate;
    @FXML
    private ComboBox<LocalTime> cmbTime;
    @FXML
    private Label lblMessage;
    @FXML
    private Button btnSave;

    private WorkshopService service;
    private Runnable onScheduled;

    /**
     * JavaFX llama este método automáticamente al cargar el formulario.
     */
    @FXML
    private void initialize() {
        cmbTime.getItems().setAll(AppointmentService.availableTimes());
        cmbTime.setPromptText("Elige la hora");
        dpDate.setPromptText("Elige el día");
        dpDate.setEditable(false);
        // No deja elegir días pasados ni domingos
        dpDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now())
                        || date.getDayOfWeek() == DayOfWeek.SUNDAY);
            }
        });
    }

    /**
     * Le entrega al formulario el servicio que se va a agendar.
     *
     * @param selected     servicio elegido en Servicios
     * @param workshopName nombre del taller que lo ofrece
     * @param whenScheduled se ejecuta después de guardar y cerrar el formulario
     */
    public void setup(WorkshopService selected, String workshopName, Runnable whenScheduled) {
        this.service = selected;
        this.onScheduled = whenScheduled;
        lblService.setText(selected.getName());
        lblWorkshop.setText("en " + workshopName);
    }

    @FXML
    private void save() {
        try {
            AppServices.appointments().schedule(Session.getUser(), service.getId(),
                    dpDate.getValue(), cmbTime.getValue());
            Navigator.closeForm(btnSave);
            if (onScheduled != null) {
                onScheduled.run();
            }
        } catch (InvalidDataException | PersistenceException e) {
            lblMessage.setText(e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        Navigator.closeForm(btnSave);
    }
}
