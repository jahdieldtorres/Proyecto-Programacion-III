package autoparts.model;

/**
 * Condición física de un componente ofrecido en el inventario. Los valores
 * coinciden con los que se guardan en {@code inventory.csv}.
 *
 * @author Héctor Guevara
 */
public enum Condition {
    NUEVO,
    USADO,
    REACONDICIONADO
}