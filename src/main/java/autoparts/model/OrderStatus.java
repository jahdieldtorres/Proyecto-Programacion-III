package autoparts.model;

/**
 * Estados de un pedido. Son los mismos textos que acepta la columna
 * ORDERS.STATUS de la base de datos.
 *
 * @author Héctor Guevara
 */
public enum OrderStatus {
    PENDIENTE,
    CONFIRMADO,
    CANCELADO
}