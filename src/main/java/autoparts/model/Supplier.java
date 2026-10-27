
package autoparts.model;

import java.time.Duration;

public class Supplier extends Partner {

    private Duration deliveryTime;

    public Supplier(int partnerId, String companyName, String taxId,
            String email, String phone, int addressId,
            PartnerStatus status, Duration deliveryTime) {
        super(partnerId, companyName, taxId, email, phone, addressId, status);
        this.deliveryTime = deliveryTime;
    }

    public Duration getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(Duration deliveryTime) {
        this.deliveryTime = deliveryTime;
    }
}
