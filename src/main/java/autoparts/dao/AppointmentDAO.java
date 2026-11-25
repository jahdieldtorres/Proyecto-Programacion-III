package autoparts.dao;

import autoparts.model.Appointment;
import autoparts.model.AppointmentStatus;

import java.util.List;
import java.util.Optional;

/**
 * Operaciones para guardar y consultar las citas de los clientes con los
 * talleres.
 *
 * AppointmentService depende de esta interfaz y no de cómo se guardan los
 * datos. Hoy la implementa AppointmentOracleDAO.
 *
 * @author Héctor Guevara
 */
public interface AppointmentDAO {

    /**
     * Guarda una cita nueva y le asigna al objeto el identificador que generó la base.
     *
     * @param appointment cita a guardar
     */
    void save(Appointment appointment);

    /**
     * Busca una cita por su identificador.
     *
     * @param id identificador de la cita
     * @return la cita, o vacío si no existe
     */
    Optional<Appointment> findById(int id);

    /**
     * Devuelve las citas de un cliente, de la más próxima a la más antigua.
     *
     * @param userId identificador del cliente
     * @return lista de citas del cliente
     */
    List<Appointment> findByUser(int userId);

    /**
     * Cambia el estado de una cita.
     *
     * @param id     identificador de la cita
     * @param status estado nuevo
     * @return true si la cita existía y se actualizó
     */
    boolean updateStatus(int id, AppointmentStatus status);
}
