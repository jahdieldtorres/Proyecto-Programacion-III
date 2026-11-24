package autoparts.model;

/**
 * Un repuesto tal como se muestra en el catálogo del cliente: el componente
 * más el resumen de sus ofertas (precio más bajo y cuántos almacenes lo
 * tienen), su foto y si sirve para el vehículo elegido.
 *
 * No se guarda en la base de datos: lo arma CatalogService cada vez que se
 * consulta el catálogo.
 *
 * @author Jahdiel Torres
 */
public class CatalogItem {

    private final Component component;
    private final int lowestPrice;
    private final int storeCount;
    private final String imageUrl;
    private final Boolean compatible;

    /**
     * @param component   el repuesto
     * @param lowestPrice precio más bajo entre los almacenes (0 si nadie lo tiene)
     * @param storeCount  cuántos almacenes lo tienen con stock
     * @param imageUrl    dirección de la foto principal, o null si no tiene
     * @param compatible  true o false si se eligió vehículo; null si no se eligió
     */
    public CatalogItem(Component component, int lowestPrice, int storeCount,
            String imageUrl, Boolean compatible) {
        this.component = component;
        this.lowestPrice = lowestPrice;
        this.storeCount = storeCount;
        this.imageUrl = imageUrl;
        this.compatible = compatible;
    }

    public Component getComponent() { return component; }

    public int getLowestPrice() { return lowestPrice; }

    public int getStoreCount() { return storeCount; }

    public String getImageUrl() { return imageUrl; }

    public Boolean getCompatible() { return compatible; }

    /** Indica si al menos un almacén tiene este repuesto con stock. */
    public boolean isAvailable() {
        return storeCount > 0;
    }

    /** Indica si se sabe que el repuesto sirve para el vehículo elegido. */
    public boolean isCompatible() {
        return Boolean.TRUE.equals(compatible);
    }
}
