package autoparts.model;

import autoparts.util.FileUtil;

import java.time.LocalDate;

/**
 * Socio de ORIGEN: una empresa que participa en el portal, ya sea como
 * proveedor de repuestos ({@link Supplier}) o como taller ({@link Workshop}).
 * Los campos comunes se guardan en {@code partners.csv}; cada subclase agrega
 * los suyos y define su propia línea con {@link #toLine()}.
 *
 * @author Héctor Guevara
 */
public abstract class Partner {

    /** Valor del campo tipo para un proveedor en {@code partners.csv}. */
    public static final String TYPE_SUPPLIER = "PROVEEDOR";

    /** Valor del campo tipo para un taller en {@code partners.csv}. */
    public static final String TYPE_WORKSHOP = "TALLER";

    /**
     * Cantidad de campos comunes (tipo, id, nombre, nit, teléfono, estado,
     * fecha, dirección, latitud, longitud).
     */
    protected static final int COMMON_FIELDS = 10;

    private int id;
    private String companyName;
    private String taxId;
    private String phone;
    private Status status;
    private LocalDate registrationDate;
    private String address;
    private double latitude;
    private double longitude;
    private String description;
    private String imageFile;

    /**
     * Crea un socio con sus campos comunes.
     *
     * @param id               identificador del socio
     * @param companyName      nombre de la compañía
     * @param taxId            NIT de la compañía
     * @param phone            teléfono de contacto
     * @param status           estado del socio
     * @param registrationDate fecha de registro
     * @param address          dirección del socio en Valledupar
     * @param latitude         latitud en grados (por ejemplo 10.46)
     * @param longitude        longitud en grados (por ejemplo -73.25)
     */
    protected Partner(int id, String companyName, String taxId, String phone,
            Status status, LocalDate registrationDate,
            String address, double latitude, double longitude) {
        this.id = id;
        this.companyName = companyName;
        this.taxId = taxId;
        this.phone = phone;
        this.status = status;
        this.registrationDate = registrationDate;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Lee los campos comunes desde los campos ya separados de una línea.
     *
     * @param fields campos de la línea de {@code partners.csv}
     */
    protected Partner(String[] fields) {
        this.id = Integer.parseInt(fields[1].trim());
        this.companyName = fields[2];
        this.taxId = fields[3];
        this.phone = fields[4];
        this.status = Status.valueOf(fields[5].trim());
        this.registrationDate = LocalDate.parse(fields[6].trim());
        this.address = fields[7];
        this.latitude = Double.parseDouble(fields[8].trim());
        this.longitude = Double.parseDouble(fields[9].trim());
    }

    /**
     * Devuelve la línea de este socio para {@code partners.csv}: el tipo
     * primero, luego los campos comunes y al final los propios del subtipo.
     *
     * @return línea con los campos separados por "|"
     */
    public abstract String toLine();

    /**
     * Arma el comienzo de la línea (campos comunes) para que lo complete cada subclase.
     *
     * @param type valor del campo tipo
     * @param own  campos propios del subtipo
     * @return línea completa con los campos separados por "|"
     */
    protected String lineWith(String type, Object... own) {
        Object[] all = new Object[COMMON_FIELDS + own.length];
        all[0] = type;
        all[1] = id;
        all[2] = companyName;
        all[3] = taxId;
        all[4] = phone;
        all[5] = status;
        all[6] = registrationDate;
        all[7] = address;
        all[8] = latitude;
        all[9] = longitude;
        System.arraycopy(own, 0, all, COMMON_FIELDS, own.length);
        return FileUtil.join(all);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    /**
     * Descripción corta del socio (máximo 300 caracteres).
     *
     * @return la descripción, o null si no tiene
     */
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    /**
     * Nombre del archivo de la foto del socio dentro de {@code images/partners/}.
     *
     * @return el nombre del archivo, o null si no tiene foto
     */
    public String getImageFile() { return imageFile; }
    public void setImageFile(String imageFile) { this.imageFile = imageFile; }
}