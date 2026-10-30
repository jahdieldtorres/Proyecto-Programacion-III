package autoparts.model;

/**
 * Estado de un registro (socio, tarifa o afiliación). Los valores coinciden
 * con los que se guardan en los archivos de datos. Eliminar un registro es
 * pasarlo a {@link #INACTIVO} (borrado lógico).
 *
 * @author Héctor Guevara
 */
public enum Status {
    ACTIVO,
    INACTIVO
}