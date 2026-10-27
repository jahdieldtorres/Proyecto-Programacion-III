
package autoparts.service;

import autoparts.dao.PartnerRepository;
import autoparts.model.Partner;
import autoparts.model.Supplier;
import autoparts.model.Workshop;

import java.util.List;
import java.util.Optional;

public class PartnerService {

    private final PartnerRepository repository;

    public PartnerService(PartnerRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("Partner repository is required.");
        }
        this.repository = repository;
    }

    public void register(Partner partner) {
        validate(partner);

        if (repository.findById(partner.getPartnerId()).isPresent()) {
            throw new IllegalArgumentException("Partner ID already exists.");
        }
        if (repository.findByTaxId(partner.getTaxId()).isPresent()) {
            throw new IllegalArgumentException("Tax ID already exists.");
        }

        repository.save(partner);
    }

    public Optional<Partner> findById(int id) {
        return repository.findById(id);
    }

    public Optional<Partner> findByTaxId(String taxId) {
        return repository.findByTaxId(taxId);
    }

    public List<Partner> findAll() {
        return repository.findAll();
    }

    public List<Supplier> findAllSuppliers() {
        return repository.findAllSuppliers();
    }

    public List<Workshop> findAllWorkshops() {
        return repository.findAllWorkshops();
    }

    public void update(Partner partner) {
        validate(partner);

        Partner existing = repository.findById(partner.getPartnerId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Partner does not exist."));

        if (!existing.getClass().equals(partner.getClass())) {
            throw new IllegalArgumentException("Cannot change partner type.");
        }

        Optional<Partner> duplicate = repository.findByTaxId(partner.getTaxId());
        if (duplicate.isPresent()
                && duplicate.get().getPartnerId() != partner.getPartnerId()) {
            throw new IllegalArgumentException("Tax ID belongs to another partner.");
        }

        if (!repository.update(partner)) {
            throw new IllegalStateException("Could not update partner.");
        }
    }

    public void delete(int id) {
        if (!repository.delete(id)) {
            throw new IllegalArgumentException("Partner does not exist.");
        }
    }

    private void validate(Partner partner) {
        if (partner == null) {
            throw new IllegalArgumentException("Partner is required.");
        }
        if (partner.getPartnerId() <= 0) {
            throw new IllegalArgumentException("Partner ID must be positive.");
        }
        if (partner.getAddressId() <= 0) {
            throw new IllegalArgumentException("Address ID must be positive.");
        }

        requireText(partner.getCompanyName(), "Company name");
        requireText(partner.getTaxId(), "Tax ID");
        requireText(partner.getEmail(), "Email");
        requireText(partner.getPhone(), "Phone");

        if (partner.getStatus() == null) {
            throw new IllegalArgumentException("Partner status is required.");
        }

        switch (partner) {
            case Supplier supplier -> {
                if (supplier.getDeliveryTime() == null
                        || supplier.getDeliveryTime().isNegative()) {
                    throw new IllegalArgumentException(
                            "Delivery time cannot be negative or null.");
                }
            }
            case Workshop workshop -> {
                requireText(workshop.getSpecialtyDescription(), "Specialty");
                if (workshop.getAvailableSpaces() < 0) {
                    throw new IllegalArgumentException(
                            "Available spaces cannot be negative.");
                }
            }
            default -> throw new IllegalArgumentException("Unsupported partner type.");
        }
    }

    private void requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required.");
        }
    }
}
