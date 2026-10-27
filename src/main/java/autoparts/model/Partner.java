
package autoparts.model;

public abstract class Partner {

    private int partnerId;
    private String companyName;
    private String taxId;
    private String email;
    private String phone;
    private int addressId;
    private PartnerStatus status;

    public Partner(int partnerId, String companyName, String taxId,
            String email, String phone, int addressId,
            PartnerStatus status) {
        this.partnerId = partnerId;
        this.companyName = companyName;
        this.taxId = taxId;
        this.email = email;
        this.phone = phone;
        this.addressId = addressId;
        this.status = status;
    }

    public int getPartnerId() { return partnerId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getTaxId() { return taxId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public int getAddressId() { return addressId; }
    public void setAddressId(int addressId) { this.addressId = addressId; }
    public PartnerStatus getStatus() { return status; }
    public void setStatus(PartnerStatus status) { this.status = status; }
}
