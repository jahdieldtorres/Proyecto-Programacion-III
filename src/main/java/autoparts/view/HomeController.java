package autoparts.view;

import autoparts.AppServices;
import autoparts.exception.PersistenceException;
import autoparts.model.CatalogItem;
import autoparts.model.Brand;
import autoparts.model.Category;
import autoparts.model.NearbyPartner;
import autoparts.model.Status;
import autoparts.model.Partner;
import autoparts.model.Workshop;
import autoparts.model.Vehicle;
import autoparts.service.Session;
import autoparts.service.UserLocationService;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Controlador de Inicio (Home.fxml).
 *
 * Tiene estas partes, de arriba hacia abajo: el banner con imágenes que pasan
 * solas, el selector de vehículo (marca → modelo → año), la franja de
 * confianza, las categorías, los repuestos destacados, los talleres aliados,
 * "Cerca de ti", las marcas que se desplazan y el pie de página. Los datos
 * los pide a los servicios.
 *
 * @author Jahdiel Torres
 */
public class HomeController {

    /** Imágenes del banner (carpeta images/banner). Para cambiarlas basta con reemplazar los archivos. */
    private static final String[] BANNER_IMAGES = {
        "images/banner/banner1.jpg", "images/banner/banner2.jpg", "images/banner/banner3.jpg"
    };
    private static final String[] BANNER_TITLES = {
        "Encuentra el repuesto exacto para tu vehículo",
        "Compara precios entre los almacenes de Valledupar",
        "Llantas, rines y más, con la mejor oferta"
    };
    private static final String[] BANNER_TEXTS = {
        "Elige tu carro o moto y mira solo lo que le sirve.",
        "Precio, envío y garantía de cada almacén, lado a lado.",
        "Agrega al carrito y coordina la entrega por WhatsApp."
    };
    private static final Duration SLIDE_TIME = Duration.seconds(5);
    private static final int FEATURED_COUNT = 5;

    @FXML
    private StackPane bannerBox;
    @FXML
    private ImageView imgBanner;
    @FXML
    private Label lblBannerTitle;
    @FXML
    private Label lblBannerText;
    @FXML
    private HBox dotsBox;
    @FXML
    private ComboBox<String> cmbBrand;
    @FXML
    private ComboBox<String> cmbModel;
    @FXML
    private ComboBox<Integer> cmbYear;
    @FXML
    private Label lblVehicleHint;
    @FXML
    private HBox trustStrip;
    @FXML
    private FlowPane categoriesBox;
    @FXML
    private FlowPane featuredBox;
    @FXML
    private Label lblMessage;
    @FXML
    private VBox workshopsBox;
    @FXML
    private Label lblNearFrom;
    @FXML
    private VBox nearbyBox;
    @FXML
    private Pane brandsViewport;
    @FXML
    private HBox brandsRow;

    private final List<Image> bannerImages = new ArrayList<>();
    private final List<Button> dots = new ArrayList<>();
    private int slide;
    private Timeline autoplay;
    private TranslateTransition brandsScroll;
    private List<Vehicle> vehicles = new ArrayList<>();

    /**
     * JavaFX llama este método automáticamente al cargar la pantalla.
     */
    @FXML
    private void initialize() {
        setUpBanner();
        buildTrustStrip();
        try {
            loadVehicles();
            loadCategories();
            loadFeatured();
            loadWorkshopsAndNearby();
            loadBrands();
        } catch (PersistenceException e) {
            lblMessage.setText("No se pudieron leer los datos: " + e.getMessage());
        }
    }

    // ---------- 1. Banner ----------

    /**
     * Carga las imágenes, crea los puntos de abajo y arranca el cambio automático.
     */
    private void setUpBanner() {
        for (String path : BANNER_IMAGES) {
            bannerImages.add(new Image(getClass().getResource(path).toExternalForm()));
        }
        // La imagen ocupa todo el ancho del banner; lo que sobra se recorta con esquinas redondeadas.
        // setManaged(false): la imagen no cambia el tamaño del banner, solo lo llena.
        imgBanner.setManaged(false);
        imgBanner.fitWidthProperty().bind(bannerBox.widthProperty());
        Rectangle clip = new Rectangle();
        clip.setArcWidth(28);
        clip.setArcHeight(28);
        clip.widthProperty().bind(bannerBox.widthProperty());
        clip.heightProperty().bind(bannerBox.heightProperty());
        bannerBox.setClip(clip);

        for (int i = 0; i < bannerImages.size(); i++) {
            int index = i;
            Button dot = new Button();
            dot.getStyleClass().add("banner-dot");
            dot.setOnAction(event -> showSlide(index));
            dots.add(dot);
            dotsBox.getChildren().add(dot);
        }
        showSlide(0);

        autoplay = new Timeline(new KeyFrame(SLIDE_TIME, event -> showSlide(slide + 1)));
        autoplay.setCycleCount(Animation.INDEFINITE);
        autoplay.play();
        // Al salir de Inicio se detiene, para que no siga corriendo en segundo plano
        bannerBox.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null) {
                autoplay.stop();
                if (brandsScroll != null) {
                    brandsScroll.stop();
                }
            }
        });
    }

    /**
     * Muestra la imagen y el texto número {@code index} con un desvanecido.
     * Si se pasa del último, vuelve al primero.
     */
    private void showSlide(int index) {
        slide = (index + bannerImages.size()) % bannerImages.size();
        imgBanner.setImage(bannerImages.get(slide));
        lblBannerTitle.setText(BANNER_TITLES[slide]);
        lblBannerText.setText(BANNER_TEXTS[slide]);
        for (int i = 0; i < dots.size(); i++) {
            dots.get(i).getStyleClass().remove("banner-dot-active");
            if (i == slide) {
                dots.get(i).getStyleClass().add("banner-dot-active");
            }
        }
        FadeTransition fade = new FadeTransition(Duration.millis(500), imgBanner);
        fade.setFromValue(0.3);
        fade.setToValue(1);
        fade.play();
    }

    // ---------- 2. Elige tu vehículo ----------

    /**
     * Lee los vehículos una vez y llena la lista de marcas.
     */
    private void loadVehicles() {
        vehicles = AppServices.vehicles().search("");
        TreeSet<String> brands = new TreeSet<>(); // TreeSet: sin repetidos y en orden alfabético
        for (Vehicle vehicle : vehicles) {
            brands.add(vehicle.getBrand().getName());
        }
        cmbBrand.getItems().setAll(brands);
        Vehicle chosen = Session.getVehicle();
        lblVehicleHint.setText(chosen == null
                ? "Elige marca, modelo y año para ver solo lo que le sirve a tu vehículo."
                : "Vehículo elegido: " + ShopController.vehicleName(chosen));
    }

    @FXML
    private void brandChosen() {
        TreeSet<String> models = new TreeSet<>();
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getBrand().getName().equals(cmbBrand.getValue())) {
                models.add(vehicle.getModelName());
            }
        }
        cmbModel.getItems().setAll(models);
        cmbYear.getItems().clear();
    }

    @FXML
    private void modelChosen() {
        TreeSet<Integer> years = new TreeSet<>();
        for (Vehicle vehicle : vehicles) {
            if (matches(vehicle, cmbBrand.getValue(), cmbModel.getValue())) {
                years.add(vehicle.getYearRelease());
            }
        }
        cmbYear.getItems().setAll(years);
        if (years.size() == 1) {
            cmbYear.setValue(years.first());
        }
    }

    /**
     * Guarda el vehículo elegido en la sesión y abre el catálogo filtrado por él.
     */
    @FXML
    private void searchByVehicle() {
        Vehicle chosen = null;
        for (Vehicle vehicle : vehicles) {
            if (matches(vehicle, cmbBrand.getValue(), cmbModel.getValue())
                    && cmbYear.getValue() != null && vehicle.getYearRelease() == cmbYear.getValue()) {
                chosen = vehicle;
            }
        }
        if (chosen == null) {
            lblVehicleHint.setText("Elige marca, modelo y año.");
            return;
        }
        Session.setVehicle(chosen);
        DashboardController.openCatalogWith("");
    }

    private boolean matches(Vehicle vehicle, String brand, String model) {
        return vehicle.getBrand().getName().equals(brand) && vehicle.getModelName().equals(model);
    }

    // ---------- 3. Franja de confianza ----------

    private void buildTrustStrip() {
        trustStrip.getChildren().addAll(
                trustItem(Icons.COMPARE, "Compara precios locales", "Precio, envío y garantía de cada almacén"),
                trustItem(Icons.LOCATION, "Almacenes de Valledupar", "Con dirección y ubicación en el mapa"),
                trustItem(Icons.WRENCH, "Instalación en talleres aliados", "Servicios con tarifa publicada"));
    }

    private HBox trustItem(String icon, String title, String subtitle) {
        StackPane circle = new StackPane(Icons.create(icon));
        circle.getStyleClass().add("trust-icon");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("trust-title");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("secondary-text");
        HBox item = new HBox(12, circle, new VBox(2, titleLabel, subtitleLabel));
        item.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(item, Priority.ALWAYS);
        return item;
    }

    // ---------- 4. Categorías ----------

    private void loadCategories() {
        for (Category category : AppServices.components().findCategories()) {
            Label name = new Label(category.getName());
            name.getStyleClass().add("category-name");
            StackPane icon = new StackPane(Icons.forCategory(category.getName()));
            icon.getStyleClass().add("category-icon");
            VBox tile = new VBox(10, icon, name);
            tile.setAlignment(Pos.CENTER);
            tile.getStyleClass().add("category-tile");
            tile.setOnMouseClicked(event -> {
                ShopController.setPendingCategory(category.getId());
                DashboardController.openCatalogWith("");
            });
            categoriesBox.getChildren().add(tile);
        }
    }

    // ---------- 5. Destacados ----------

    private void loadFeatured() {
        for (CatalogItem item : AppServices.catalog().featured(FEATURED_COUNT)) {
            featuredBox.getChildren().add(new ComponentCard(item, () -> DashboardController.openDetail(item)));
        }
    }

    // ---------- 6. Talleres aliados y Cerca de ti ----------

    /**
     * Lee los socios una vez: los talleres van a la columna izquierda y los
     * dos más cercanos (almacén o taller) a "Cerca de ti".
     */
    private void loadWorkshopsAndNearby() {
        List<Workshop> workshops = AppServices.partners().findAllWorkshops();
        for (Workshop workshop : workshops) {
            if (workshop.getStatus() == Status.ACTIVO) {
                workshopsBox.getChildren().add(miniRow(Icons.WRENCH, workshop.getCompanyName(),
                        workshop.getSpecialty() + " · " + workshop.getAddress(),
                        workshop.getBayCount() + " bahías", DashboardController::openServices));
            }
        }

        List<Partner> partners = new ArrayList<>(AppServices.partners().findAllSuppliers());
        partners.addAll(workshops);
        List<NearbyPartner> nearby = AppServices.locations().sortByDistance(Session.getUser(), partners);
        lblNearFrom.setText("Desde " + (AppServices.locations().hasChosen(Session.getUser())
                ? "tu ubicación" : UserLocationService.DEFAULT_LOCATION_NAME));
        for (NearbyPartner item : nearby.subList(0, Math.min(2, nearby.size()))) {
            Partner partner = item.partner();
            nearbyBox.getChildren().add(miniRow(item.isWorkshop() ? Icons.WRENCH : Icons.STORE,
                    partner.getCompanyName(), (item.isWorkshop() ? "Taller · " : "Almacén · ") + partner.getAddress(),
                    MapController.distanceText(item.distanceKm()), DashboardController::openMap));
        }
    }

    /**
     * Fila pequeña con ícono, título, detalle y un dato a la derecha.
     */
    private HBox miniRow(String icon, String title, String detail, String value, Runnable onClick) {
        StackPane iconBox = new StackPane(Icons.create(icon));
        iconBox.getStyleClass().add("category-icon");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("offer-supplier");
        Label detailLabel = new Label(detail);
        detailLabel.getStyleClass().add("secondary-text");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("distance");
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox row = new HBox(12, iconBox, new VBox(2, titleLabel, detailLabel), space, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().addAll("card", "mini-row");
        row.setOnMouseClicked(event -> onClick.run());
        return row;
    }

    // ---------- 7. Marcas que se desplazan ----------

    /**
     * Pone las marcas dos veces seguidas y las mueve a la izquierda sin parar.
     * Cuando la primera copia sale de la vista, la segunda está justo donde
     * empezó, así que el movimiento parece infinito.
     */
    private void loadBrands() {
        List<Brand> brands = AppServices.components().findBrands();
        for (int copy = 0; copy < 2; copy++) {
            for (Brand brand : brands) {
                Label word = new Label(brand.getName().toUpperCase());
                word.getStyleClass().add("brand-word");
                word.setMinWidth(Region.USE_PREF_SIZE);
                brandsRow.getChildren().add(word);
            }
        }
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(brandsViewport.widthProperty());
        clip.heightProperty().bind(brandsViewport.heightProperty());
        brandsViewport.setClip(clip);

        // El ancho de la fila solo se conoce cuando JavaFX la dibuja por primera vez
        brandsRow.widthProperty().addListener((observable, oldWidth, width) -> {
            if (brandsScroll == null && width.doubleValue() > 0) {
                double half = (width.doubleValue() + brandsRow.getSpacing()) / 2;
                brandsScroll = new TranslateTransition(Duration.seconds(half / 40), brandsRow);
                brandsScroll.setFromX(0);
                brandsScroll.setToX(-half);
                brandsScroll.setInterpolator(Interpolator.LINEAR);
                brandsScroll.setCycleCount(Animation.INDEFINITE);
                brandsScroll.play();
            }
        });
    }

    @FXML
    private void goToMap() {
        DashboardController.openMap();
    }

    @FXML
    private void goToCatalog() {
        DashboardController.openCatalogWith("");
    }

    @FXML
    private void goToServices() {
        DashboardController.openServices();
    }
}
