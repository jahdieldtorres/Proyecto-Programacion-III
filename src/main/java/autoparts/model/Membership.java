
package autoparts.model;

import java.time.LocalDate;

public class Membership {

    private int membershipId;
    private int partnerId;
    private int rateId;
    private LocalDate startDate;
    private LocalDate expirationDate;
    private MembershipStatus status;

    public Membership(int membershipId, int partnerId, int rateId,
            LocalDate startDate, LocalDate expirationDate,
            MembershipStatus status) {
        this.membershipId = membershipId;
        this.partnerId = partnerId;
        this.rateId = rateId;
        this.startDate = startDate;
        this.expirationDate = expirationDate;
        this.status = status;
    }

    public int getMembershipId() { return membershipId; }
    public int getPartnerId() { return partnerId; }
    public void setPartnerId(int partnerId) { this.partnerId = partnerId; }
    public int getRateId() { return rateId; }
    public void setRateId(int rateId) { this.rateId = rateId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }
    public MembershipStatus getStatus() { return status; }
    public void setStatus(MembershipStatus status) { this.status = status; }
}
