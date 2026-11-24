package autoparts.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pedido de un cliente: su estado y sus líneas ({@link OrderItem}).
 * Corresponde a una fila de ORDERS más sus filas de ORDER_ITEMS.
 *
 * El total no se guarda como atributo: se calcula con las líneas, así nunca
 * queda distinto de ellas. El DAO escribe {@link #getTotal()} en ORDERS.TOTAL.
 *
 * @author Héctor Guevara
 */
public class Order {

    private int id;
    private int userId;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private final List<OrderItem> items = new ArrayList<>();

    /**
     * Crea un pedido sin líneas; se agregan con {@link #addItem(OrderItem)}.
     *
     * @param id        identificador (0 si todavía no se guarda)
     * @param userId    identificador del cliente que lo hizo
     * @param orderDate fecha y hora del pedido
     * @param status    estado del pedido
     */
    public Order(int id, int userId, LocalDateTime orderDate, OrderStatus status) {
        this.id = id;
        this.userId = userId;
        this.orderDate = orderDate;
        this.status = status;
    }

    public void addItem(OrderItem item) {
        items.add(item);
    }

    /**
     * Líneas del pedido. La lista no se puede modificar: para eso está addItem.
     */
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Suma de precio por cantidad de todas las líneas, sin envío.
     */
    public int getSubtotal() {
        return items.stream().mapToInt(OrderItem::getSubtotal).sum();
    }

    /**
     * Suma del envío cobrado en todas las líneas.
     */
    public int getShippingTotal() {
        return items.stream().mapToInt(OrderItem::getShippingCost).sum();
    }

    /**
     * Total del pedido: subtotal más envíos.
     */
    public int getTotal() {
        return getSubtotal() + getShippingTotal();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}