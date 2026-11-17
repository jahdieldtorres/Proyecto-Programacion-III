package autoparts.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Carrito de compras del cliente: una lista de líneas ({@link CartItem}) que
 * vive en memoria mientras dura la sesión (ver Session.getCart()).
 *
 * Regla del envío: cada proveedor cobra su envío UNA sola vez por pedido. Si
 * en el carrito hay varias ofertas del mismo proveedor, se cobra el envío más
 * alto de ellas. El total del carrito es el precio por cantidad de todas las
 * líneas más el envío de cada proveedor.
 *
 * Esta clase solo guarda y calcula. Las validaciones para el usuario (stock
 * disponible, oferta activa) van en el servicio de pedidos, no aquí.
 *
 * @author Héctor Guevara
 */
public class Cart {

    private final List<CartItem> items = new ArrayList<>();

    /**
     * Agrega una oferta al carrito. Si esa oferta ya estaba, suma la cantidad
     * a la línea que existe en lugar de crear otra.
     *
     * @param offer    oferta elegida
     * @param quantity unidades a agregar (mínimo 1)
     * @throws IllegalArgumentException si la oferta es nula o la cantidad es menor que 1
     *                                  (error de programación, no del usuario)
     */
    public void add(Offer offer, int quantity) {
        if (offer == null || quantity < 1) {
            throw new IllegalArgumentException("Se necesita una oferta y una cantidad de al menos 1.");
        }
        CartItem existing = find(offer.getItem().getId());
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
        } else {
            items.add(new CartItem(offer, quantity));
        }
    }

    /**
     * Quita del carrito la línea de una oferta. Si no estaba, no pasa nada.
     *
     * @param inventoryId identificador de la existencia (la oferta)
     */
    public void remove(int inventoryId) {
        items.removeIf(item -> item.getInventoryId() == inventoryId);
    }

    /**
     * Cambia la cantidad de una línea. Si la cantidad es menor que 1, la línea
     * se quita. Si la oferta no está en el carrito, no pasa nada.
     *
     * @param inventoryId identificador de la existencia (la oferta)
     * @param quantity    cantidad nueva
     */
    public void setQuantity(int inventoryId, int quantity) {
        if (quantity < 1) {
            remove(inventoryId);
            return;
        }
        CartItem existing = find(inventoryId);
        if (existing != null) {
            existing.setQuantity(quantity);
        }
    }

    /**
     * Vacía el carrito (por ejemplo, después de confirmar el pedido).
     */
    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Líneas del carrito, en el orden en que se agregaron. La lista no se
     * puede modificar: para eso están add, remove y setQuantity.
     */
    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Total de unidades en el carrito (suma de las cantidades de todas las líneas).
     */
    public int getUnitCount() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    /**
     * Suma de precio por cantidad de todas las líneas, sin envío.
     */
    public int getSubtotal() {
        return items.stream().mapToInt(CartItem::getSubtotal).sum();
    }

    /**
     * Subtotal sin envío de cada proveedor, para la pantalla del carrito.
     *
     * @return mapa nuevo: ID del proveedor → subtotal, en el orden en que aparecen
     */
    public Map<Integer, Integer> getSubtotalBySupplier() {
        Map<Integer, Integer> subtotals = new LinkedHashMap<>();
        for (CartItem item : items) {
            subtotals.merge(item.getSupplierId(), item.getSubtotal(), Integer::sum);
        }
        return subtotals;
    }

    /**
     * Envío de cada proveedor: el más alto entre sus líneas, cobrado una vez.
     *
     * @return mapa nuevo: ID del proveedor → envío, en el orden en que aparecen
     */
    public Map<Integer, Integer> getShippingBySupplier() {
        Map<Integer, Integer> shipping = new LinkedHashMap<>();
        for (CartItem item : items) {
            shipping.merge(item.getSupplierId(), item.getShippingCost(), Math::max);
        }
        return shipping;
    }

    /**
     * Suma del envío de todos los proveedores del carrito.
     */
    public int getShippingTotal() {
        return getShippingBySupplier().values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Total a pagar: precio por cantidad de todas las líneas más el envío de
     * cada proveedor.
     */
    public int getTotal() {
        return getSubtotal() + getShippingTotal();
    }

    /**
     * Convierte el carrito en un pedido nuevo en estado PENDIENTE, listo para
     * que OrderService.checkout() lo guarde. El envío de cada proveedor queda
     * en su primera línea y las demás líneas del mismo proveedor llevan 0, así
     * el total del pedido ({@link Order#getTotal()}) es igual al del carrito.
     *
     * @param userId identificador del cliente que hace el pedido
     * @return pedido sin ID (lo asigna la base) con una línea por cada línea del carrito
     */
    public Order toOrder(int userId) {
        Order order = new Order(0, userId, LocalDateTime.now(), OrderStatus.PENDIENTE);
        // Mapa nuevo: se puede ir quitando cada proveedor al cobrarle su envío.
        Map<Integer, Integer> pendingShipping = getShippingBySupplier();
        for (CartItem item : items) {
            Integer shipping = pendingShipping.remove(item.getSupplierId());
            order.addItem(new OrderItem(0, 0, item.getInventoryId(), item.getQuantity(),
                    item.getUnitPrice(), shipping == null ? 0 : shipping));
        }
        return order;
    }

    private CartItem find(int inventoryId) {
        for (CartItem item : items) {
            if (item.getInventoryId() == inventoryId) {
                return item;
            }
        }
        return null;
    }
}