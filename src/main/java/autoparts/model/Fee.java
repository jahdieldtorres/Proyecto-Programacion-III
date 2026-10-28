package autoparts.model;

import autoparts.util.FileUtil;

import java.time.LocalDate;


public class Fee {

    private int id;
    private int serviceId;
    private int amount;
    private LocalDate startDate;
    private Status status;

    /**
     * Crea una tarifa.
     *
     * @param id        identificador de la tarifa
     * @param serviceId identificador del servicio (en {@code services.csv})
     * @param amount    valor en pesos colombianos
     * @param startDate fecha desde la que rige
     * @param status    estado de la tarifa
     */
    public Fee(int id, int serviceId, int amount, LocalDate startDate, Status status) {
        this.id = id;
        this.serviceId = serviceId;
        this.amount = amount;
        this.startDate = startDate;
        this.status = status;
    }


    public Fee(String line) {
        String[] f = FileUtil.split(line);
        if (f.length != 5) {
            throw new IllegalArgumentException("Una tarifa tiene 5 campos.");
        }
        this.id = Integer.parseInt(f[0].trim());
        this.serviceId = Integer.parseInt(f[1].trim());
        this.amount = Integer.parseInt(f[2].trim());
        this.startDate = LocalDate.parse(f[3].trim());
        this.status = Status.valueOf(f[4].trim());
    }

    public String toLine() {
        return FileUtil.join(id, serviceId, amount, startDate, status);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}