package autoparts.model;

/**
 * Estado de una cita con un taller. Los valores coinciden con los que se
 * guardan en la columna STATUS de APPOINTMENTS. Cancelar una cita es pasarla a
 * {@link #CANCELADA} (no se borra).
 *
 * @author Héctor Guevara
 */
public enum AppointmentStatus {
    /** El cliente la agendó y el taller todavía no la confirma. */
    PENDIENTE,
    /** El taller confirmó la cita. */
    CONFIRMADA,
    /** El cliente (o el taller) la canceló. */
    CANCELADA
}
