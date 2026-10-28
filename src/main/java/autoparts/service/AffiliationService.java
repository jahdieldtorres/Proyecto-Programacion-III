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

/**
 * Reglas de negocio de las afiliaciones entre proveedores y talleres: ambos
 * deben existir, estar activos y ser del tipo correcto, y la misma pareja no
 * puede tener dos afiliaciones activas.
 *
 * @author Héctor Guevara
 */
public class AffiliationService {

    private final AffiliationDAO affiliationDAO;
    private final PartnerDAO partnerDAO;

    /**
     * Crea el servicio con sus DAO.
     *
     * @param affiliationDAO acceso a datos de las afiliaciones
     * @param partnerDAO     acceso a datos de los socios, para validar proveedor y taller
     */
    public AffiliationService(AffiliationDAO affiliationDAO, PartnerDAO partnerDAO) {
        this.affiliationDAO = affiliationDAO;
        this.partnerDAO = partnerDAO;
    }

    /**
     * Registra una afiliación nueva.
     *
     * @param affiliation afiliación a registrar
     * @throws IllegalArgumentException si algún dato no es válido o la pareja ya está afiliada
     */
    public void register(Affiliation affiliation) {
        validate(affiliation, 0);
        affiliation.setId(affiliationDAO.nextId());
        affiliationDAO.save(affiliation);
    }

    /**
     * Actualiza una afiliación.
     *
     * @param affiliation afiliación con los datos nuevos
     * @throws IllegalArgumentException si la afiliación no existe o algún dato no es válido
     */
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

    /**
     * Elimina una afiliación con borrado lógico: pasa a estado {@code INACTIVO}.
     *
     * @param id identificador de la afiliación
     * @throws IllegalArgumentException si la afiliación no existe
     */
    public void delete(int id) {
        Affiliation affiliation = affiliationDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La afiliación no existe."));
        affiliation.setStatus(Status.INACTIVO);
        affiliationDAO.update(affiliation);
    }

    /**
     * Busca una afiliación por su identificador.
     *
     * @param id identificador de la afiliación
     * @return la afiliación, o vacío si no existe
     */
    public Optional<Affiliation> findById(int id) {
        return affiliationDAO.findById(id);
    }

    /**
     * Devuelve las afiliaciones de un taller.
     *
     * @param workshopId identificador del taller
     * @return lista de afiliaciones del taller
     */
    public List<Affiliation> findByWorkshop(int workshopId) {
        return affiliationDAO.findByWorkshop(workshopId);
    }

    /**
     * Devuelve las afiliaciones de un proveedor.
     *
     * @param supplierId identificador del proveedor
     * @return lista de afiliaciones del proveedor
     */
    public List<Affiliation> findBySupplier(int supplierId) {
        return affiliationDAO.findBySupplier(supplierId);
    }

    /**
     * Devuelve todas las afiliaciones.
     *
     * @return lista de afiliaciones
     */
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