package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.InvalidDataException;
import autoparts.exception.PersistenceException;
import autoparts.model.Appointment;
import autoparts.model.AppointmentStatus;
import autoparts.model.Partner;
import autoparts.model.WorkshopService;
import autoparts.service.Session;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Controlador de Mis citas (Appointments.fxml).
 *
 * Lista las citas del cliente de la sesión con el servicio, el taller, la
 * fecha y el estado, y deja cancelar las que todavía no han pasado. Solo
 * llama a servicios a través de AppServices.
 *
 * @author Héctor Guevara
 */
public class AppointmentsController {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy · HH:mm", Locale.of("es", "CO"));

    @FXML
    private Label lblCount;
    @FXML
    private Label lblMessage;
    @FXML
    private VBox listBox;
    @FXML
    private VBox emptyBox;

    /**
     * JavaFX llama este método automáticamente al cargar la pantalla.
     */
    @FXML
    private void initialize() {
        load();
    }

    /**
     * Lee las citas del cliente y dibuja una tarjeta por cada una.
     */
    private void load() {
        listBox.getChildren().clear();
        try {
            List<Appointment> appointments = AppServices.appointments().findByUser(Session.getUser());
            for (Appointment appointment : appointments) {
                listBox.getChildren().add(appointmentCard(appointment));
            }
            boolean empty = appointments.isEmpty();
            emptyBox.setVisible(empty);
            emptyBox.setManaged(empty);
            lblCount.setText(empty ? "" : appointments.size() == 1 ? "1 cita" : appointments.size() + " citas");
        } catch (PersistenceException e) {
            lblMessage.setText(e.getMessage());
        }
    }

    /**
     * Tarjeta de una cita: servicio y taller a la izquierda, fecha, estado y
     * botón Cancelar a la derecha.
     */
    private HBox appointmentCard(Appointment appointment) {
        String serviceName = "Servicio #" + appointment.getServiceId();
        String workshopName = "";
        try {
            WorkshopService service = AppServices.serviceCatalog().findById(appointment.getServiceId()).orElse(null);
            if (service != null) {
                serviceName = service.getName();
                workshopName = AppServices.partners().findById(service.getWorkshopId())
                        .map(Partner::getCompanyName).orElse("");
            }
        } catch (PersistenceException e) {
            lblMessage.setText(e.getMessage());
        }

        Label name = new Label(serviceName);
        name.getStyleClass().add("offer-supplier");
        Label workshop = new Label(workshopName);
        workshop.getStyleClass().add("secondary-text");
        Label date = new Label(capitalize(appointment.getDateTime().format(DATE_FORMAT)));
        VBox left = new VBox(4, name, workshop, date);

        Label status = new Label(statusText(appointment.getStatus()));
        status.getStyleClass().add(statusStyle(appointment.getStatus()));
        VBox right = new VBox(8, status);
        right.setAlignment(Pos.CENTER_RIGHT);
        if (canCancel(appointment)) {
            Button cancel = new Button("Cancelar cita");
            cancel.getStyleClass().add("secondary-button");
            cancel.setOnAction(event -> cancel(appointment));
            right.getChildren().add(cancel);
        }

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox card = new HBox(16, left, space, right);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("card");
        return card;
    }

    private boolean canCancel(Appointment appointment) {
        return appointment.getStatus() != AppointmentStatus.CANCELADA
                && appointment.getDateTime().isAfter(LocalDateTime.now());
    }

    private void cancel(Appointment appointment) {
        lblMessage.setText("");
        try {
            AppServices.appointments().cancel(Session.getUser(), appointment.getId());
            load();
        } catch (InvalidDataException | PersistenceException e) {
            lblMessage.setText(e.getMessage());
        }
    }

    private String statusText(AppointmentStatus status) {
        return switch (status) {
            case PENDIENTE -> "Pendiente";
            case CONFIRMADA -> "Confirmada";
            case CANCELADA -> "Cancelada";
        };
    }

    private String statusStyle(AppointmentStatus status) {
        return switch (status) {
            case PENDIENTE -> "badge-amber";
            case CONFIRMADA -> "badge-green";
            case CANCELADA -> "badge-gray";
        };
    }

    /** Pone la primera letra en mayúscula ("lunes 1 de..." → "Lunes 1 de..."). */
    private String capitalize(String text) {
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    @FXML
    private void goToServices() {
        DashboardController.openServices();
    }
}
