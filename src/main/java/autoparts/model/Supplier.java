package autoparts.model;

import autoparts.util.FileUtil;

import java.time.LocalDate;


public class Supplier extends Partner {

    private int deliveryDays;

    /**
     * Crea un proveedor.
     *
     * @param id               identificador del socio
     * @param companyName      nombre de la compañía
     * @param taxId            NIT de la compañía
     * @param phone            teléfono de contacto
     * @param status           estado del socio
     * @param registrationDate fecha de registro
     * @param deliveryDays     días de entrega
     */
    public Supplier(int id, String companyName, String taxId, String phone,
            Status status, LocalDate registrationDate, int deliveryDays) {
        super(id, companyName, taxId, phone, status, registrationDate);
        this.deliveryDays = deliveryDays;
    }


    public Supplier(String line) {
        this(FileUtil.split(line));
    }

    private Supplier(String[] fields) {
        super(fields);
        if (fields.length != COMMON_FIELDS + 1) {
            throw new IllegalArgumentException("Un proveedor tiene " + (COMMON_FIELDS + 1) + " campos.");
        }
        this.deliveryDays = Integer.parseInt(fields[7].trim());
    }

    @Override
    public String toLine() {
        return lineWith(TYPE_SUPPLIER, deliveryDays);
    }

    public int getDeliveryDays() { return deliveryDays; }
    public void setDeliveryDays(int deliveryDays) { this.deliveryDays = deliveryDays; }
}