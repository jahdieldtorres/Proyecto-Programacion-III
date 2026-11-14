package autoparts.model;

/**
 * Rol de un usuario del sistema. Los valores coinciden con los que se guardan
 * en data/users.csv.
 *
 * ADMIN gestiona el catálogo, los socios, el inventario y los usuarios.
 * CLIENTE consulta el catálogo, compara ofertas y hace pedidos.
 *
 * @author Jahdiel Torres
 */
public enum Role {
    ADMIN,
    CLIENTE
}
