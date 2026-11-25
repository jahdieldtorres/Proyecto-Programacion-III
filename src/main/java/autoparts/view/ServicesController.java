package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.PersistenceException;
import autoparts.model.Fee;
import autoparts.model.Status;
import autoparts.model.Workshop;
import autoparts.model.WorkshopService;
import autoparts.service.Session;
import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Controlador de Servicios (Services.fxml).
 *
 * Muestra los talleres aliados activos con sus servicios y la tarifa vigente
 * de cada uno. Cada taller tiene el botón "Escribir por WhatsApp", igual que
 * en el mapa, y cada servicio el botón "Agendar" para pedir una cita (si es
 * invitado, se abre la ventana "Regístrate"). Solo llama a servicios a través
 * de AppServices.
 *
 * @author Jahdiel Torres
 * @author Héctor Guevara (servicios y tarifas)
 */
public class ServicesController {

    @FXML
    private FlowPane workshopsBox;
    @FXML
    private Label lblMessage;

    /**
     * JavaFX llama este método automáticamente al cargar la pantalla.
     */
    @FXML
    private void initialize() {
        try {
            for (Workshop workshop : AppServices.partners().findAllWorkshops()) {
                if (workshop.getStatus() == Status.ACTIVO) {
                    workshopsBox.getChildren().add(workshopCard(workshop));
                }
            }
        } catch (PersistenceException e) {
            lblMessage.setText(e.getMessage());
        }
        if (workshopsBox.getChildren().isEmpty() && lblMessage.getText().isEmpty()) {
            lblMessage.setText("Todavía no hay talleres aliados con servicios.");
        }
    }

    /**
     * Tarjeta de un taller: nombre, especialidad, dirección, teléfono, la lista
     * de sus servicios con tarifa y el botón de WhatsApp.
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

        Label servicesTitle = new Label("Servicios");
        servicesTitle.getStyleClass().add("field-label");
        VBox servicesBox = new VBox(8);
        loadServices(workshop, servicesBox);

        Button whatsapp = new Button("Escribir por WhatsApp");
        whatsapp.getStyleClass().add("whatsapp-button");
        whatsapp.setOnAction(event -> openWhatsApp(workshop));

        VBox card = new VBox(8, icon, name, specialty, address, phone, servicesTitle, servicesBox, whatsapp);
        card.getStyleClass().addAll("card", "workshop-card");
        card.setPrefWidth(340);
        return card;
    }

    /**
     * Agrega una fila por cada servicio activo del taller. Si no tiene, avisa.
     */
    private void loadServices(Workshop workshop, VBox servicesBox) {
        try {
            List<WorkshopService> services = AppServices.serviceCatalog().findActiveByWorkshop(workshop.getId());
            for (WorkshopService service : services) {
                servicesBox.getChildren().add(serviceRow(workshop, service));
            }
            if (services.isEmpty()) {
                Label empty = new Label("Este taller todavía no tiene servicios publicados.");
                empty.getStyleClass().add("secondary-text");
                servicesBox.getChildren().add(empty);
            }
        } catch (PersistenceException e) {
            lblMessage.setText(e.getMessage());
        }
    }

    /**
     * Fila de un servicio: nombre y descripción a la izquierda; tarifa vigente
     * y botón Agendar a la derecha.
     */
    private HBox serviceRow(Workshop workshop, WorkshopService service) {
        Label name = new Label(service.getName());
        name.setWrapText(true);
        VBox left = new VBox(2, name);
        if (service.getDescription() != null && !service.getDescription().isBlank()) {
            Label description = new Label(service.getDescription());
            description.getStyleClass().add("secondary-text");
            description.setWrapText(true);
            left.getChildren().add(description);
        }

        Label price = new Label(priceText(service));
        price.getStyleClass().add("product-price");

        Button schedule = new Button("Agendar");
        schedule.getStyleClass().add("secondary-button");
        schedule.setOnAction(event -> schedule(workshop, service, schedule));
        VBox right = new VBox(4, price, schedule);
        right.setAlignment(Pos.CENTER_RIGHT);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox row = new HBox(10, left, space, right);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /**
     * Botón Agendar: abre el formulario de la cita. Si es invitado, abre la
     * ventana "Regístrate para comprar". Al agendar, lleva a Mis citas.
     */
    private void schedule(Workshop workshop, WorkshopService service, Button source) {
        if (!Session.requireAccount()) {
            GuestDialog.show(source);
            return;
        }
        Navigator.openForm("AppointmentForm.fxml", "Agendar cita",
                (AppointmentFormController form) -> form.setup(service, workshop.getCompanyName(),
                        DashboardController::openAppointments));
    }

    /**
     * Texto de la tarifa vigente del servicio, o "Consultar" si no tiene.
     */
    private String priceText(WorkshopService service) {
        try {
            return AppServices.fees().findCurrentByService(service.getId())
                    .map(Fee::getAmount)
                    .map(Formats::money)
                    .orElse("Consultar");
        } catch (PersistenceException e) {
            lblMessage.setText(e.getMessage());
            return "Consultar";
        }
    }

    private void openWhatsApp(Workshop workshop) {
        String text = "Hola " + workshop.getCompanyName()
                + ", te escribo desde ORIGEN. Quiero consultar por sus servicios.";
        if (!WhatsApp.open(workshop, text)) {
            lblMessage.setText("No se pudo abrir WhatsApp. Escribe al " + workshop.getPhone() + ".");
        }
    }
}
