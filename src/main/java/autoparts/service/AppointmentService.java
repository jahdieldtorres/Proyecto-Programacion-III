package autoparts.service;

import autoparts.dao.AppointmentDAO;
import autoparts.dao.WorkshopServiceDAO;
import autoparts.exception.InvalidDataException;
import autoparts.model.Appointment;
import autoparts.model.AppointmentStatus;
import autoparts.model.Status;
import autoparts.model.User;
import autoparts.model.WorkshopService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Reglas de negocio de la agenda de citas con los talleres.
 *
 * Una cita se agenda en horas en punto, de 8:00 a 17:00, de lunes a sábado,
 * con al menos una hora de anticipación, para un servicio activo. Un cliente
 * no puede tener dos citas vigentes para el mismo servicio a la misma hora.
 * Las validaciones lanzan {@link InvalidDataException}.
 *
 * @author Héctor Guevara
 */
public class AppointmentService {

    /** Primera hora en la que se puede agendar (8:00). */
    public static final int FIRST_HOUR = 8;

    /** Última hora en la que se puede agendar (17:00). */
    public static final int LAST_HOUR = 17;

    /** Horas de anticipación mínimas para agendar. */
    private static final long MIN_NOTICE_HOURS = 1;

    private final AppointmentDAO appointmentDAO;
    private final WorkshopServiceDAO serviceDAO;

    /**
     * Crea el servicio con sus DAO.
     *
     * @param appointmentDAO acceso a datos de las citas
     * @param serviceDAO     acceso a datos de los servicios de los talleres
     */
    public AppointmentService(AppointmentDAO appointmentDAO, WorkshopServiceDAO serviceDAO) {
        this.appointmentDAO = appointmentDAO;
        this.serviceDAO = serviceDAO;
    }

    /**
     * Horas en las que se puede agendar una cita (de 8:00 a 17:00, en punto).
     *
     * @return lista de horas, de la más temprana a la más tarde
     */
    public static List<LocalTime> availableTimes() {
        List<LocalTime> times = new ArrayList<>();
        for (int hour = FIRST_HOUR; hour <= LAST_HOUR; hour++) {
            times.add(LocalTime.of(hour, 0));
        }
        return times;
    }

    /**
     * Agenda una cita para el cliente. Queda en estado {@code PENDIENTE}.
     *
     * @param user      cliente que agenda (no puede ser invitado)
     * @param serviceId servicio que pide
     * @param date      día de la cita
     * @param time      hora de la cita
     * @return la cita guardada, con su identificador
     * @throws InvalidDataException si algún dato no cumple las reglas
     */
    public Appointment schedule(User user, int serviceId, LocalDate date, LocalTime time) {
        if (user == null) {
            throw new InvalidDataException("Debes iniciar sesión para agendar una cita.");
        }
        if (date == null || time == null) {
            throw new InvalidDataException("Elige el día y la hora de la cita.");
        }
        if (!availableTimes().contains(time)) {
            throw new InvalidDataException("La hora debe ser en punto, entre las "
                    + FIRST_HOUR + ":00 y las " + LAST_HOUR + ":00.");
        }
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new InvalidDataException("Los talleres no atienden los domingos.");
        }
        LocalDateTime when = LocalDateTime.of(date, time);
        if (when.isBefore(LocalDateTime.now().plusHours(MIN_NOTICE_HOURS))) {
            throw new InvalidDataException("La cita debe ser con al menos una hora de anticipación.");
        }
        WorkshopService service = serviceDAO.findById(serviceId).orElse(null);
        if (service == null || service.getStatus() != Status.ACTIVO) {
            throw new InvalidDataException("El servicio no existe o ya no está disponible.");
        }
        for (Appointment existing : appointmentDAO.findByUser(user.getId())) {
            if (existing.getServiceId() == serviceId
                    && existing.getDateTime().equals(when)
                    && existing.getStatus() != AppointmentStatus.CANCELADA) {
                throw new InvalidDataException("Ya tienes una cita para ese servicio a esa hora.");
            }
        }
        Appointment appointment = new Appointment(0, user.getId(), serviceId, when, AppointmentStatus.PENDIENTE);
        appointmentDAO.save(appointment);
        return appointment;
    }

    /**
     * Devuelve las citas del cliente, de la más próxima a la más antigua.
     *
     * @param user cliente de la sesión
     * @return lista de sus citas (vacía si es invitado)
     */
    public List<Appointment> findByUser(User user) {
        if (user == null) {
            return List.of();
        }
        return appointmentDAO.findByUser(user.getId());
    }

    /**
     * Cancela una cita del cliente. Solo se pueden cancelar las citas propias,
     * que no estén canceladas y que todavía no hayan pasado.
     *
     * @param user          cliente de la sesión
     * @param appointmentId cita que se quiere cancelar
     * @throws InvalidDataException si la cita no existe, no es del cliente o ya no se puede cancelar
     */
    public void cancel(User user, int appointmentId) {
        if (user == null) {
            throw new InvalidDataException("Debes iniciar sesión para cancelar una cita.");
        }
        Appointment appointment = appointmentDAO.findById(appointmentId)
                .orElseThrow(() -> new InvalidDataException("La cita no existe."));
        if (appointment.getUserId() != user.getId()) {
            throw new InvalidDataException("Solo puedes cancelar tus propias citas.");
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELADA) {
            throw new InvalidDataException("La cita ya está cancelada.");
        }
        if (appointment.getDateTime().isBefore(LocalDateTime.now())) {
            throw new InvalidDataException("La cita ya pasó y no se puede cancelar.");
        }
        appointmentDAO.updateStatus(appointmentId, AppointmentStatus.CANCELADA);
    }
}
