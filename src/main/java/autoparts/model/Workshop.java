
package autoparts.model;

public class Workshop extends Partner {

    private String specialtyDescription;
    private int availableSpaces;

    public Workshop(int partnerId, String companyName, String taxId,
            String email, String phone, int addressId,
            PartnerStatus status, String specialtyDescription,
            int availableSpaces) {
        super(partnerId, companyName, taxId, email, phone, addressId, status);
        this.specialtyDescription = specialtyDescription;
        this.availableSpaces = availableSpaces;
    }

    public String getSpecialtyDescription() {
        return specialtyDescription;
    }

    public void setSpecialtyDescription(String specialtyDescription) {
        this.specialtyDescription = specialtyDescription;
    }

    public int getAvailableSpaces() {
        return availableSpaces;
    }

    public void setAvailableSpaces(int availableSpaces) {
        this.availableSpaces = availableSpaces;
    }
}
