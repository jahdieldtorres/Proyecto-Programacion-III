package autoparts.model;

/**
 * Servicio que ofrece un taller (por ejemplo "Cambio de pastillas de freno").
 * Su precio no está aquí: es la tarifa vigente ({@link Fee}) del servicio.
 *
 * Se llama WorkshopService para no confundirlo con las clases de la capa de
 * servicios (paquete {@code autoparts.service}). Corresponde a la tabla
 * SERVICES de Oracle.
 *
 * @author Héctor Guevara
 */
public class WorkshopService {

    private int id;
    private int workshopId;
    private String name;
    private String description;
    private Status status;

    /**
     * Crea un servicio de taller.
     *
     * @param id          identificador del servicio
     * @param workshopId  identificador del taller (socio) que lo ofrece
     * @param name        nombre del servicio
     * @param description qué incluye el servicio; puede ser null
     * @param status      estado del servicio
     */
    public WorkshopService(int id, int workshopId, String name, String description, Status status) {
        this.id = id;
        this.workshopId = workshopId;
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getWorkshopId() { return workshopId; }
    public void setWorkshopId(int workshopId) { this.workshopId = workshopId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
