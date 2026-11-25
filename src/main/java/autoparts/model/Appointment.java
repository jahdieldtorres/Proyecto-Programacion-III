package autoparts.model;

import java.time.LocalDateTime;

/**
 * Cita que un cliente agenda con un taller para un servicio. Guarda el
 * servicio (el taller se obtiene de él), la fecha con la hora y su estado.
 * Corresponde a la tabla APPOINTMENTS de Oracle.
 *
 * @author Héctor Guevara
 */
public class Appointment {

    private int id;
    private int userId;
    private int serviceId;
    private LocalDateTime dateTime;
    private AppointmentStatus status;

    /**
     * Crea una cita.
     *
     * @param id        identificador de la cita (0 si todavía no se guarda)
     * @param userId    identificador del cliente que la agendó
     * @param serviceId identificador del servicio que pidió
     * @param dateTime  fecha y hora de la cita
     * @param status    estado de la cita
     */
    public Appointment(int id, int userId, int serviceId, LocalDateTime dateTime, AppointmentStatus status) {
        this.id = id;
        this.userId = userId;
        this.serviceId = serviceId;
        this.dateTime = dateTime;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }
    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
}
