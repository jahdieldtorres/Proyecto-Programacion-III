package autoparts.service;

import autoparts.dao.PartnerDAO;
import autoparts.model.Partner;
import autoparts.model.Status;
import autoparts.model.Supplier;
import autoparts.model.Workshop;

import java.util.List;
import java.util.Optional;


public class PartnerService {

    private final PartnerDAO partnerDAO;


    public PartnerService(PartnerDAO partnerDAO) {
        this.partnerDAO = partnerDAO;
    }

    public void register(Partner partner) {
        validate(partner);
        if (partnerDAO.findByTaxId(partner.getTaxId()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un socio con ese NIT.");
        }
        partner.setId(partnerDAO.nextId());
        partnerDAO.save(partner);
    }

 
    public void update(Partner partner) {
        validate(partner);
        Partner existing = partnerDAO.findById(partner.getId())
                .orElseThrow(() -> new IllegalArgumentException("El socio no existe."));
        if (!existing.getClass().equals(partner.getClass())) {
            throw new IllegalArgumentException("No se puede cambiar el tipo de socio.");
        }
        Optional<Partner> sameTaxId = partnerDAO.findByTaxId(partner.getTaxId());
        if (sameTaxId.isPresent() && sameTaxId.get().getId() != partner.getId()) {
            throw new IllegalArgumentException("Ese NIT pertenece a otro socio.");
        }
        partnerDAO.update(partner);
    }


    public void delete(int id) {
        Partner partner = partnerDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El socio no existe."));
        partner.setStatus(Status.INACTIVO);
        partnerDAO.update(partner);
    }


    public Optional<Partner> findById(int id) {
        return partnerDAO.findById(id);
    }

    public Optional<Partner> findByTaxId(String taxId) {
        return partnerDAO.findByTaxId(taxId);
    }


    public List<Partner> findAll() {
        return partnerDAO.findAll();
    }


    public List<Supplier> findAllSuppliers() {
        return partnerDAO.findAll().stream()
                .filter(Supplier.class::isInstance)
                .map(Supplier.class::cast)
                .toList();
    }


    public List<Workshop> findAllWorkshops() {
        return partnerDAO.findAll().stream()
                .filter(Workshop.class::isInstance)
                .map(Workshop.class::cast)
                .toList();
    }

    private void validate(Partner partner) {
        if (partner == null) {
            throw new IllegalArgumentException("El socio es obligatorio.");
        }
        partner.setCompanyName(cleanText(partner.getCompanyName(), "El nombre de la compañía"));
        partner.setTaxId(cleanText(partner.getTaxId(), "El NIT"));
        partner.setPhone(cleanText(partner.getPhone(), "El teléfono"));
        if (partner.getStatus() == null) {
            throw new IllegalArgumentException("El estado es obligatorio.");
        }
        if (partner.getRegistrationDate() == null) {
            throw new IllegalArgumentException("La fecha de registro es obligatoria.");
        }
        if (partner instanceof Supplier supplier) {
            if (supplier.getDeliveryDays() < 0) {
                throw new IllegalArgumentException("Los días de entrega no pueden ser negativos.");
            }
        } else if (partner instanceof Workshop workshop) {
            workshop.setSpecialty(cleanText(workshop.getSpecialty(), "La especialidad"));
            if (workshop.getBayCount() < 0) {
                throw new IllegalArgumentException("El número de bahías no puede ser negativo.");
            }
        } else {
            throw new IllegalArgumentException("Tipo de socio no soportado.");
        }
    }

    private String cleanText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " es obligatorio.");
        }
        if (value.contains("|") || value.contains("\n") || value.contains("\r")) {
            throw new IllegalArgumentException(field + " no puede contener '|' ni saltos de línea.");
        }
        return value.trim();
    }
}