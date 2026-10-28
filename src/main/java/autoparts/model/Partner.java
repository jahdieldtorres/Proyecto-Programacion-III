package autoparts.model;

import autoparts.util.FileUtil;

import java.time.LocalDate;


public abstract class Partner {


    public static final String TYPE_SUPPLIER = "PROVEEDOR";

    public static final String TYPE_WORKSHOP = "TALLER";


    protected static final int COMMON_FIELDS = 7;

    private int id;
    private String companyName;
    private String taxId;
    private String phone;
    private Status status;
    private LocalDate registrationDate;

    /**
     * Crea un socio con sus campos comunes.
     *
     * @param id               identificador del socio
     * @param companyName      nombre de la compañía
     * @param taxId            NIT de la compañía
     * @param phone            teléfono de contacto
     * @param status           estado del socio
     * @param registrationDate fecha de registro
     */
    protected Partner(int id, String companyName, String taxId, String phone,
            Status status, LocalDate registrationDate) {
        this.id = id;
        this.companyName = companyName;
        this.taxId = taxId;
        this.phone = phone;
        this.status = status;
        this.registrationDate = registrationDate;
    }


    protected Partner(String[] fields) {
        this.id = Integer.parseInt(fields[1].trim());
        this.companyName = fields[2];
        this.taxId = fields[3];
        this.phone = fields[4];
        this.status = Status.valueOf(fields[5].trim());
        this.registrationDate = LocalDate.parse(fields[6].trim());
    }


    public abstract String toLine();

 
    protected String lineWith(String type, Object... own) {
        Object[] all = new Object[COMMON_FIELDS + own.length];
        all[0] = type;
        all[1] = id;
        all[2] = companyName;
        all[3] = taxId;
        all[4] = phone;
        all[5] = status;
        all[6] = registrationDate;
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
}