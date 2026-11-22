package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.PersistenceException;
import autoparts.model.Status;
import autoparts.model.Workshop;
import javafx.animation.FadeTransition;
import javafx.animation.Animation;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Controlador de Servicios (Services.fxml).
 *
 * La sección está en construcción: por ahora muestra los talleres aliados
 * activos con su especialidad y dirección, para que se vea lo que viene.
 *
 * @author Jahdiel Torres
 */
public class ServicesController {

    @FXML
    private HBox noticeBox;
    @FXML
    private FlowPane workshopsBox;
    @FXML
    private Label lblMessage;

    /**
     * JavaFX llama este método automáticamente al cargar la pantalla.
     */
    @FXML
    private void initialize() {
        pulseNotice();
        try {
            for (Workshop workshop : AppServices.partners().findAllWorkshops()) {
                if (workshop.getStatus() == Status.ACTIVO) {
                    workshopsBox.getChildren().add(workshopCard(workshop));
                }
            }
        } catch (PersistenceException e) {
            lblMessage.setText(e.getMessage());
        }
    }

    /**
     * El aviso de "en construcción" late suavemente para llamar la atención.
     */
    private void pulseNotice() {
        FadeTransition pulse = new FadeTransition(Duration.seconds(1.2), noticeBox);
        pulse.setFromValue(1);
        pulse.setToValue(0.75);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();
    }

    /**
     * Tarjeta de un taller: nombre, especialidad, bahías, dirección y teléfono.
     */
    private VBox workshopCard(Workshop workshop) {
        StackPane icon = new StackPane(Icons.create(Icons.WRENCH));
        icon.getStyleClass().add("category-icon");
        Label name = new Label(workshop.getCompanyName());
        name.getStyleClass().add("offer-supplier");
        Label specialty = new Label(workshop.getSpecialty() + " · " + workshop.getBayCount() + " bahías");
        specialty.getStyleClass().add("badge-green");
        Label address = new Label(workshop.getAddress());
        address.getStyleClass().add("secondary-text");
        address.setWrapText(true);
        Label phone = new Label("Tel. " + workshop.getPhone());
        phone.getStyleClass().add("secondary-text");
        Button schedule = new Button("Agendar · próximamente");
        schedule.setDisable(true);
        schedule.getStyleClass().add("secondary-button");

        VBox card = new VBox(8, icon, name, specialty, address, phone, schedule);
        card.getStyleClass().addAll("card", "workshop-card");
        card.setPrefWidth(300);
        return card;
    }
}
