package autoparts.model;

/**
 * Oferta de un proveedor por un componente: junta los datos del
 * {@link Supplier} (nombre y días de entrega) con los de su {@link Inventory}
 * (precio, envío, garantía y condición). No se guarda en ningún archivo: el
 * comparador la arma al buscar.
 *
 * @author Héctor Guevara
 */
public class Offer {

    private final Supplier supplier;
    private final Inventory item;

    /**
     * Crea una oferta.
     *
     * @param supplier proveedor que ofrece el componente
     * @param item     existencia del proveedor para ese componente
     */
    public Offer(Supplier supplier, Inventory item) {
        this.supplier = supplier;
        this.item = item;
    }

    /**
     * Costo total para el cliente: precio del componente más el envío.
     *
     * @return precio + envío, en pesos
     */
    public int getTotalCost() {
        return item.getPrice() + item.getShippingCost();
    }

    public Supplier getSupplier() { return supplier; }
    public Inventory getItem() { return item; }
    public String getSupplierName() { return supplier.getCompanyName(); }
    public int getPrice() { return item.getPrice(); }
    public int getShippingCost() { return item.getShippingCost(); }
    public int getWarrantyMonths() { return item.getWarrantyMonths(); }
    public int getDeliveryDays() { return supplier.getDeliveryDays(); }
    public int getStock() { return item.getStock(); }
    public String getCondition() { return item.getCondition(); }
}