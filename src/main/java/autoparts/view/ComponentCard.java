package autoparts.view;

import autoparts.model.CatalogItem;
import autoparts.model.Component;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

/**
 * Tarjeta de un repuesto en el catálogo y en Inicio: foto, marca, referencia,
 * nombre, compatibilidad, precio más bajo y cuántos almacenes lo tienen.
 *
 * Es un componente visual reutilizable: se crea con {@code new ComponentCard(item, accion)}
 * y la acción se ejecuta al hacer clic (por ejemplo, abrir el detalle).
 *
 * @author Jahdiel Torres
 */
public class ComponentCard extends VBox {

    public static final double WIDTH = 208;
    private static final double PHOTO_HEIGHT = 140;

    /**
     * @param item   el repuesto con su resumen de ofertas
     * @param onOpen lo que pasa al hacer clic en la tarjeta
     */
    public ComponentCard(CatalogItem item, Runnable onOpen) {
        Component component = item.getComponent();
        getStyleClass().add("product-card");
        setPrefWidth(WIDTH);
        setMaxWidth(WIDTH);

        VBox info = new VBox(5,
                brandRow(component),
                nameLabel(component),
                compatibilityBadge(item),
                priceLabel(item),
                storesPill(item));
        info.getStyleClass().add("product-card-info");

        getChildren().addAll(photo(item.getImageUrl()), info);
        setOnMouseClicked(event -> onOpen.run());
    }

    /**
     * Foto del repuesto, recortada a un tamaño fijo. Si no tiene foto, muestra un aviso.
     */
    private StackPane photo(String imageUrl) {
        StackPane box = new StackPane();
        box.getStyleClass().add("product-photo");
        box.setPrefSize(WIDTH, PHOTO_HEIGHT);
        box.setMinHeight(PHOTO_HEIGHT);

        if (imageUrl == null) {
            Label empty = new Label("Sin foto");
            empty.getStyleClass().add("secondary-text");
            box.getChildren().add(empty);
        } else {
            ImageView view = new ImageView(new Image(imageUrl, WIDTH * 1.5, 0, true, true, true));
            view.setPreserveRatio(true);
            view.setFitWidth(WIDTH);
            box.getChildren().add(view);
        }
        // El recorte evita que una foto alta se salga de la tarjeta
        box.setClip(new Rectangle(WIDTH, PHOTO_HEIGHT));
        return box;
    }

    private HBox brandRow(Component component) {
        Label brand = new Label(component.getBrand().getName().toUpperCase());
        brand.getStyleClass().add("product-brand");
        Label reference = new Label(component.getReference());
        reference.getStyleClass().add("reference-text");
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox row = new HBox(brand, space, reference);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Label nameLabel(Component component) {
        Label name = new Label(component.getName());
        name.getStyleClass().add("product-name");
        name.setWrapText(true);
        name.setMinHeight(38);
        return name;
    }

    /**
     * Solo aparece si el cliente eligió vehículo: dice si sirve o si está por confirmar.
     */
    private Label compatibilityBadge(CatalogItem item) {
        Label badge = new Label();
        if (item.getCompatible() == null) {
            badge.setVisible(false);
            badge.setManaged(false);
        } else if (item.isCompatible()) {
            badge.setText("✓ Compatible con tu vehículo");
            badge.getStyleClass().add("badge-blue");
        } else {
            badge.setText("Compatibilidad por confirmar");
            badge.getStyleClass().add("badge-gray");
        }
        return badge;
    }

    private Label priceLabel(CatalogItem item) {
        Label price = new Label();
        price.getStyleClass().add("product-price");
        if (!item.isAvailable()) {
            price.setText("Sin stock");
            price.getStyleClass().add("product-price-empty");
        } else if (item.getStoreCount() == 1) {
            price.setText(Formats.money(item.getLowestPrice()));
        } else {
            price.setText("Desde " + Formats.money(item.getLowestPrice()));
        }
        return price;
    }

    private Label storesPill(CatalogItem item) {
        Label pill = new Label(Formats.stores(item.getStoreCount()));
        pill.getStyleClass().add("pill");
        pill.setVisible(item.isAvailable());
        return pill;
    }
}
