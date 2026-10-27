
package autoparts.model;

public class Rate {

    private final int rateId;
    private String name;
    private double percentage;

    public Rate(int rateId, String name, double percentage) {
        this.rateId = rateId;
        this.name = name;
        this.percentage = percentage;
    }

    public int getRateId() { return rateId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
}
