package autoparts.model;

/**
 * Una línea de un pedido: la oferta pedida, la cantidad y los precios que
 * tenía al momento de confirmar. Corresponde a una fila de ORDER_ITEMS.
 *
 * Guarda una copia del precio y del envío (no los lee de INVENTORY) para que
 * el pedido no cambie si el proveedor cambia su precio después.
 *
 * @author Héctor Guevara
 */
public class OrderItem {

    private int id;
    private int orderId;
    private int inventoryId;
    private int quantity;
    private int unitPrice;
    private int shippingCost;

    /**
     * Crea una línea de pedido.
     *
     * @param id           identificador (0 si todavía no se guarda)
     * @param orderId      identificador del pedido al que pertenece (0 si todavía no se guarda)
     * @param inventoryId  identificador de la existencia pedida
     * @param quantity     unidades pedidas
     * @param unitPrice    precio por unidad al momento del pedido
     * @param shippingCost envío cobrado en esta línea (0 si el envío del proveedor va en otra línea)
     */
    public OrderItem(int id, int orderId, int inventoryId, int quantity,
            int unitPrice, int shippingCost) {
        this.id = id;
        this.orderId = orderId;
        this.inventoryId = inventoryId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.shippingCost = shippingCost;
    }

    /**
     * Precio de la línea sin envío: precio por unidad por la cantidad.
     */
    public int getSubtotal() {
        return unitPrice * quantity;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getInventoryId() { return inventoryId; }
    public void setInventoryId(int inventoryId) { this.inventoryId = inventoryId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getUnitPrice() { return unitPrice; }
    public void setUnitPrice(int unitPrice) { this.unitPrice = unitPrice; }
    public int getShippingCost() { return shippingCost; }
    public void setShippingCost(int shippingCost) { this.shippingCost = shippingCost; }
}