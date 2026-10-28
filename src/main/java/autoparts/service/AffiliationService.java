package autoparts.service;

import autoparts.dao.AffiliationDAO;
import autoparts.dao.PartnerDAO;
import autoparts.model.Affiliation;
import autoparts.model.Partner;
import autoparts.model.Status;
import autoparts.model.Supplier;
import autoparts.model.Workshop;

import java.util.List;
import java.util.Optional;


public class AffiliationService {

    private final AffiliationDAO affiliationDAO;
    private final PartnerDAO partnerDAO;


    public AffiliationService(AffiliationDAO affiliationDAO, PartnerDAO partnerDAO) {
        this.affiliationDAO = affiliationDAO;
        this.partnerDAO = partnerDAO;
    }

 
    public void register(Affiliation affiliation) {
        validate(affiliation, 0);
        affiliation.setId(affiliationDAO.nextId());
        affiliationDAO.save(affiliation);
    }

    public void update(Affiliation affiliation) {
        if (affiliation == null) {
            throw new IllegalArgumentException("La afiliación es obligatoria.");
        }
        if (affiliationDAO.findById(affiliation.getId()).isEmpty()) {
            throw new IllegalArgumentException("La afiliación no existe.");
        }
        validate(affiliation, affiliation.getId());
        affiliationDAO.update(affiliation);
    }


    public void delete(int id) {
        Affiliation affiliation = affiliationDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La afiliación no existe."));
        affiliation.setStatus(Status.INACTIVO);
        affiliationDAO.update(affiliation);
    }


    public Optional<Affiliation> findById(int id) {
        return affiliationDAO.findById(id);
    }


    public List<Affiliation> findByWorkshop(int workshopId) {
        return affiliationDAO.findByWorkshop(workshopId);
    }


    public List<Affiliation> findBySupplier(int supplierId) {
        return affiliationDAO.findBySupplier(supplierId);
    }


    public List<Affiliation> findAll() {
        return affiliationDAO.findAll();
    }

    private void validate(Affiliation affiliation, int ownId) {
        if (affiliation == null) {
            throw new IllegalArgumentException("La afiliación es obligatoria.");
        }
        if (affiliation.getStartDate() == null) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria.");
        }
        if (affiliation.getStatus() == null) {
            throw new IllegalArgumentException("El estado es obligatorio.");
        }
        if (affiliation.getStatus() != Status.ACTIVO) {
            return;
        }

        Partner supplier = partnerDAO.findById(affiliation.getSupplierId()).orElse(null);
        if (!(supplier instanceof Supplier) || supplier.getStatus() != Status.ACTIVO) {
            throw new IllegalArgumentException("El proveedor no existe o no está activo.");
        }
        Partner workshop = partnerDAO.findById(affiliation.getWorkshopId()).orElse(null);
        if (!(workshop instanceof Workshop) || workshop.getStatus() != Status.ACTIVO) {
            throw new IllegalArgumentException("El taller no existe o no está activo.");
        }

        boolean alreadyAffiliated = affiliationDAO.findByWorkshop(affiliation.getWorkshopId()).stream()
                .anyMatch(a -> a.getId() != ownId
                        && a.getSupplierId() == affiliation.getSupplierId()
                        && a.getStatus() == Status.ACTIVO);
        if (alreadyAffiliated) {
            throw new IllegalArgumentException("El proveedor ya está afiliado a ese taller.");
        }
    }
}