package autoparts.model;

/**
 * Una línea del carrito: una oferta de un proveedor y la cantidad que el
 * cliente quiere de ella.
 *
 * @author Héctor Guevara
 */
public class CartItem {

    private final Offer offer;
    private int quantity;

    /**
     * Crea una línea del carrito.
     *
     * @param offer    oferta elegida (proveedor + existencia)
     * @param quantity cantidad de unidades
     */
    public CartItem(Offer offer, int quantity) {
        this.offer = offer;
        this.quantity = quantity;
    }

    /**
     * Precio de la línea sin envío: precio de la oferta por la cantidad.
     */
    public int getSubtotal() {
        return getUnitPrice() * quantity;
    }

    public Offer getOffer() { return offer; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getInventoryId() { return offer.getItem().getId(); }
    public int getSupplierId() { return offer.getSupplier().getId(); }
    public int getUnitPrice() { return offer.getPrice(); }
    public int getShippingCost() { return offer.getShippingCost(); }
}