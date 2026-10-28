package autoparts.model;

import autoparts.util.FileUtil;

import java.time.LocalDate;

/**
 * Afiliación de un proveedor a un taller. Se guarda en {@code affiliations.csv}.
 *
 * @author Héctor Guevara
 */
public class Affiliation {

    private int id;
    private int supplierId;
    private int workshopId;
    private LocalDate startDate;
    private Status status;

    /**
     * Crea una afiliación.
     *
     * @param id         identificador de la afiliación
     * @param supplierId identificador del proveedor (en {@code partners.csv})
     * @param workshopId identificador del taller (en {@code partners.csv})
     * @param startDate  fecha de inicio
     * @param status     estado de la afiliación
     */
    public Affiliation(int id, int supplierId, int workshopId, LocalDate startDate, Status status) {
        this.id = id;
        this.supplierId = supplierId;
        this.workshopId = workshopId;
        this.startDate = startDate;
        this.status = status;
    }

    /**
     * Crea una afiliación desde una línea de {@code affiliations.csv}.
     *
     * @param line línea con los campos separados por "|"
     */
    public Affiliation(String line) {
        String[] f = FileUtil.split(line);
        if (f.length != 5) {
            throw new IllegalArgumentException("Una afiliación tiene 5 campos.");
        }
        this.id = Integer.parseInt(f[0].trim());
        this.supplierId = Integer.parseInt(f[1].trim());
        this.workshopId = Integer.parseInt(f[2].trim());
        this.startDate = LocalDate.parse(f[3].trim());
        this.status = Status.valueOf(f[4].trim());
    }

    /**
     * Devuelve la línea de esta afiliación para {@code affiliations.csv}.
     *
     * @return línea con los campos separados por "|"
     */
    public String toLine() {
        return FileUtil.join(id, supplierId, workshopId, startDate, status);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }
    public int getWorkshopId() { return workshopId; }
    public void setWorkshopId(int workshopId) { this.workshopId = workshopId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}