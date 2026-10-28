package autoparts.model;

import autoparts.util.FileUtil;

import java.time.LocalDate;

/**
 * Taller mecánico. Además de los datos comunes del socio, guarda su
 * especialidad y el número de bahías de trabajo.
 *
 * @author Héctor Guevara
 */
public class Workshop extends Partner {

    private String specialty;
    private int bayCount;

    /**
     * Crea un taller.
     *
     * @param id               identificador del socio
     * @param companyName      nombre de la compañía
     * @param taxId            NIT de la compañía
     * @param phone            teléfono de contacto
     * @param status           estado del socio
     * @param registrationDate fecha de registro
     * @param specialty        especialidad del taller
     * @param bayCount         número de bahías
     */
    public Workshop(int id, String companyName, String taxId, String phone,
            Status status, LocalDate registrationDate,
            String specialty, int bayCount) {
        super(id, companyName, taxId, phone, status, registrationDate);
        this.specialty = specialty;
        this.bayCount = bayCount;
    }

    /**
     * Crea un taller desde una línea de {@code partners.csv}.
     *
     * @param line línea con los campos separados por "|"
     */
    public Workshop(String line) {
        this(FileUtil.split(line));
    }

    private Workshop(String[] fields) {
        super(fields);
        if (fields.length != COMMON_FIELDS + 2) {
            throw new IllegalArgumentException("Un taller tiene " + (COMMON_FIELDS + 2) + " campos.");
        }
        this.specialty = fields[7];
        this.bayCount = Integer.parseInt(fields[8].trim());
    }

    @Override
    public String toLine() {
        return lineWith(TYPE_WORKSHOP, specialty, bayCount);
    }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public int getBayCount() { return bayCount; }
    public void setBayCount(int bayCount) { this.bayCount = bayCount; }
}