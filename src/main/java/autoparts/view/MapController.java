package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.PersistenceException;
import autoparts.model.GeoPoint;
import autoparts.model.NearbyPartner;
import autoparts.model.Partner;
import autoparts.model.Supplier;
import autoparts.model.User;
import autoparts.model.Workshop;
import autoparts.service.Session;
import autoparts.service.UserLocationService;
import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import netscape.javascript.JSObject;

/**
 * Controlador de "Talleres cerca" (Map.fxml).
 *
 * A la izquierda muestra los almacenes y talleres ordenados por distancia,
 * con botón de WhatsApp. A la derecha, un mapa real: es una página web
 * (view/map/map.html, con la librería Leaflet) dentro de un WebView.
 *
 * Cómo se comunican Java y el mapa:
 * <ul>
 *   <li>Java → mapa: {@code engine.executeScript("addMarker(...)")} llama
 *       funciones de JavaScript de la página.</li>
 *   <li>Mapa → Java: la página llama los métodos del objeto "app", que es un
 *       {@link MapBridge} que Java le entrega al terminar de cargar.</li>
 * </ul>
 *
 * @author Jahdiel Torres
 */
public class MapController {

    @FXML
    private Label lblFrom;
    @FXML
    private VBox listBox;
    @FXML
    private Label lblMessage;
    @FXML
    private WebView webView;
    @FXML
    private VBox loadingBox;
    @FXML
    private Label lblLoading;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private VBox locationBox;
    @FXML
    private Label lblPickHint;

    private WebEngine engine;
    private boolean mapReady;
    private List<NearbyPartner> nearby = new ArrayList<>();
    /**
     * Puente que recibe los avisos del mapa. Se guarda en un campo para que
     * Java no lo borre de la memoria mientras la página lo usa.
     */
    private final MapBridge bridge = new MapBridge();

    /**
     * JavaFX llama este método automáticamente al cargar la pantalla.
     */
    @FXML
    private void initialize() {
        loadPartners();
        loadMap();
    }

    // ---------- Lista ----------

    /**
     * Lee almacenes y talleres, los ordena por distancia y dibuja la lista.
     */
    private void loadPartners() {
        User user = Session.getUser();
        try {
            List<Partner> partners = new ArrayList<>(AppServices.partners().findAllSuppliers());
            partners.addAll(AppServices.partners().findAllWorkshops());
            nearby = AppServices.locations().sortByDistance(user, partners);
        } catch (PersistenceException e) {
            lblMessage.setText(e.getMessage());
        }
        lblFrom.setText("Distancias desde: " + (AppServices.locations().hasChosen(user)
                ? "tu ubicación" : UserLocationService.DEFAULT_LOCATION_NAME));
        listBox.getChildren().clear();
        for (NearbyPartner item : nearby) {
            listBox.getChildren().add(partnerCard(item));
        }
    }

    /**
     * Tarjeta de un almacén o taller. Al hacer clic, el mapa va hasta su pin.
     */
    private VBox partnerCard(NearbyPartner item) {
        Partner partner = item.partner();
        StackPane icon = new StackPane(Icons.create(item.isWorkshop() ? Icons.WRENCH : Icons.STORE));
        icon.getStyleClass().addAll("category-icon", item.isWorkshop() ? "icon-workshop" : "icon-store");

        Label name = new Label(partner.getCompanyName());
        name.getStyleClass().add("offer-supplier");
        name.setWrapText(true);
        Label type = new Label(typeText(partner));
        type.getStyleClass().add(item.isWorkshop() ? "badge-amber" : "badge-blue");
        VBox title = new VBox(4, name, type);
        HBox.setHgrow(title, Priority.ALWAYS);

        Label distance = new Label(distanceText(item.distanceKm()));
        distance.getStyleClass().add("distance");
        distance.setMinWidth(Region.USE_PREF_SIZE); // la distancia nunca se corta
        HBox header = new HBox(12, icon, title, distance);
        header.setAlignment(Pos.CENTER_LEFT);

        Label address = new Label(partner.getAddress());
        address.getStyleClass().add("secondary-text");
        address.setWrapText(true);

        Button whatsapp = new Button("Escribir por WhatsApp");
        whatsapp.getStyleClass().add("whatsapp-button");
        whatsapp.setOnAction(event -> openWhatsApp(partner));

        VBox card = new VBox(10, header, address, whatsapp);
        card.getStyleClass().addAll("card", "partner-card");
        card.setOnMouseClicked(event -> focusOnMap(partner));
        return card;
    }

    private String typeText(Partner partner) {
        if (partner instanceof Workshop workshop) {
            return "Taller · " + workshop.getSpecialty();
        }
        if (partner instanceof Supplier supplier) {
            return "Almacén · entrega en " + supplier.getDeliveryDays() + " días";
        }
        return "Socio";
    }

    /**
     * Escribe la distancia como "850 m" si es menos de un kilómetro, o "2,3 km".
     */
    static String distanceText(double km) {
        if (km < 1) {
            return Math.round(km * 1000) + " m";
        }
        return String.format(Locale.of("es", "CO"), "%.1f km", km);
    }

    /**
     * Abre WhatsApp (en el navegador o en la app de escritorio) con un mensaje listo.
     */
    private void openWhatsApp(Partner partner) {
        String phone = "57" + partner.getPhone().replaceAll("[^0-9]", "");
        String text = "Hola " + partner.getCompanyName() + ", te escribo desde ORIGEN.";
        String url = "https://wa.me/" + phone + "?text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            lblMessage.setText("No se pudo abrir WhatsApp. Escribe al " + partner.getPhone() + ".");
        }
    }

    // ---------- Mapa ----------

    /**
     * Carga la página del mapa y muestra la pantalla de carga mientras tanto.
     * La barra avanza con el progreso real de la carga del WebView.
     */
    private void loadMap() {
        engine = webView.getEngine();
        engine.setUserAgent("ORIGEN/1.0 (proyecto universitario, Valledupar)");
        progressBar.progressProperty().bind(engine.getLoadWorker().progressProperty());

        // Si el WebView cambia de tamaño, el mapa debe recalcular el suyo
        webView.widthProperty().addListener((observable, oldWidth, width) -> refreshMapSize());
        webView.heightProperty().addListener((observable, oldHeight, height) -> refreshMapSize());

        engine.getLoadWorker().stateProperty().addListener((observable, oldState, state) -> {
            if (state == Worker.State.SUCCEEDED) {
                onMapLoaded();
            } else if (state == Worker.State.FAILED) {
                showMapError("No se pudo abrir el mapa.");
            }
        });
        engine.load(getClass().getResource("map/map.html").toExternalForm());
    }

    /**
     * Cuando la página terminó de cargar: le entrega el puente, pone los
     * pines y quita la pantalla de carga con un desvanecido.
     */
    private void onMapLoaded() {
        JSObject window = (JSObject) engine.executeScript("window");
        window.setMember("app", bridge);
        mapReady = true;
        refreshMapSize();
        drawMarkers();

        FadeTransition fade = new FadeTransition(Duration.millis(400), loadingBox);
        fade.setToValue(0);
        fade.setOnFinished(event -> loadingBox.setVisible(false));
        fade.play();

        if (!AppServices.locations().hasChosen(Session.getUser())) {
            askLocation();
        }
    }

    /**
     * Pone en el mapa el punto del usuario y un pin por cada almacén o taller.
     */
    private void drawMarkers() {
        if (!mapReady) {
            return;
        }
        GeoPoint user = AppServices.locations().locationOf(Session.getUser());
        String label = AppServices.locations().hasChosen(Session.getUser())
                ? "Tú estás aquí" : UserLocationService.DEFAULT_LOCATION_NAME;
        run("clearMarkers()");
        run("setUser(" + user.latitude() + ", " + user.longitude() + ", " + js(label) + ")");
        for (NearbyPartner item : nearby) {
            Partner partner = item.partner();
            run("addMarker(" + partner.getId() + ", " + partner.getLatitude() + ", " + partner.getLongitude()
                    + ", " + js(partner.getCompanyName()) + ", " + item.isWorkshop()
                    + ", " + js(typeText(partner) + "<br>" + distanceText(item.distanceKm())) + ")");
        }
        run("fitAll()");
    }

    /**
     * Le dice al mapa que vuelva a medir su tamaño. Sin esto, cuando la
     * ventana crece, una parte del mapa se queda gris.
     */
    private void refreshMapSize() {
        if (mapReady) {
            run("refreshSize()");
        }
    }

    private void focusOnMap(Partner partner) {
        if (mapReady) {
            run("focusMarker(" + partner.getId() + ")");
        }
    }

    /** Ejecuta una función de JavaScript de la página del mapa. */
    private void run(String script) {
        engine.executeScript(script);
    }

    /**
     * Convierte un texto en un texto de JavaScript entre comillas, escapando
     * las comillas que tenga adentro.
     */
    private String js(String text) {
        return "'" + text.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }

    private void showMapError(String message) {
        progressBar.progressProperty().unbind();
        progressBar.setVisible(false);
        lblLoading.setText(message + " Revisa tu conexión a internet; la lista sí funciona.");
        lblLoading.setWrapText(true);
        loadingBox.setOpacity(1);
        loadingBox.setVisible(true);
    }

    // ---------- Ubicación ----------

    /**
     * Muestra la ventanita "¿Desde dónde buscas?".
     */
    @FXML
    private void askLocation() {
        locationBox.setVisible(true);
    }

    /**
     * El próximo clic en el mapa será la ubicación del usuario.
     */
    @FXML
    private void pickOnMap() {
        locationBox.setVisible(false);
        if (mapReady) {
            lblPickHint.setVisible(true);
            run("startPicking()");
        }
    }

    @FXML
    private void useDefaultLocation() {
        locationBox.setVisible(false);
        saveLocation(UserLocationService.DEFAULT_LOCATION);
    }

    /**
     * Guarda la ubicación para la cuenta, reordena la lista y redibuja el mapa.
     */
    private void saveLocation(GeoPoint location) {
        AppServices.locations().choose(Session.getUser(), location);
        lblPickHint.setVisible(false);
        loadPartners();
        drawMarkers();
    }

    /**
     * Objeto que la página del mapa ve como "app". Sus métodos son públicos
     * porque los llama JavaScript.
     */
    public class MapBridge {

        /**
         * El usuario hizo clic en el mapa para elegir su ubicación.
         */
        public void locationPicked(double latitude, double longitude) {
            Platform.runLater(() -> saveLocation(new GeoPoint(latitude, longitude)));
        }

        /**
         * El fondo del mapa no cargó (casi seguro, no hay internet).
         */
        public void tilesFailed() {
            Platform.runLater(() -> showMapError("El fondo del mapa no cargó."));
        }
    }
}
