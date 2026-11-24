package autoparts.model;

/**
 * Un almacén o taller junto con su distancia al cliente.
 *
 * @author Jahdiel Torres
 * @param partner    el almacén (Supplier) o el taller (Workshop)
 * @param distanceKm distancia en kilómetros desde la ubicación del cliente
 */
public record NearbyPartner(Partner partner, double distanceKm) {

    /** Indica si es un taller; si no, es un almacén. */
    public boolean isWorkshop() {
        return partner instanceof Workshop;
    }
}
