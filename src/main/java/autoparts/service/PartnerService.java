package autoparts.service;

import autoparts.dao.PartnerDAO;
import autoparts.exception.InvalidDataException;
import autoparts.model.Partner;
import autoparts.model.Status;
import autoparts.model.Supplier;
import autoparts.model.Workshop;

import java.util.List;
import java.util.Optional;

/**
 * Reglas de negocio de los socios (proveedores y talleres): validación de los
 * datos, NIT único y borrado lógico. Solo depende de la interfaz {@link PartnerDAO}.
 *
 * @author Héctor Guevara
 */
public class PartnerService {

    private final PartnerDAO partnerDAO;

    /**
     * Crea el servicio con su DAO.
     *
     * @param partnerDAO acceso a datos de los socios
     */
    public PartnerService(PartnerDAO partnerDAO) {
        this.partnerDAO = partnerDAO;
    }

    /**
     * Registra un socio nuevo. Le asigna el siguiente identificador libre.
     *
     * @param partner socio a registrar
     * @throws InvalidDataException si algún dato no es válido o el NIT ya existe
     */
    public void register(Partner partner) {
        validate(partner);
        if (partnerDAO.findByTaxId(partner.getTaxId()).isPresent()) {
            throw new InvalidDataException("Ya existe un socio con ese NIT.");
        }
        partner.setId(partnerDAO.nextId());
        partnerDAO.save(partner);
    }

    /**
     * Actualiza los datos de un socio. No se puede cambiar su tipo.
     *
     * @param partner socio con los datos nuevos
     * @throws InvalidDataException si el socio no existe, cambia de tipo,
     *                                  o algún dato no es válido
     */
    public void update(Partner partner) {
        validate(partner);
        Partner existing = partnerDAO.findById(partner.getId())
                .orElseThrow(() -> new InvalidDataException("El socio no existe."));
        if (!existing.getClass().equals(partner.getClass())) {
            throw new InvalidDataException("No se puede cambiar el tipo de socio.");
        }
        Optional<Partner> sameTaxId = partnerDAO.findByTaxId(partner.getTaxId());
        if (sameTaxId.isPresent() && sameTaxId.get().getId() != partner.getId()) {
            throw new InvalidDataException("Ese NIT pertenece a otro socio.");
        }
        partnerDAO.update(partner);
    }

    /**
     * Elimina un socio con borrado lógico: pasa a estado {@code INACTIVO}.
     *
     * @param id identificador del socio
     * @throws InvalidDataException si el socio no existe
     */
    public void delete(int id) {
        Partner partner = partnerDAO.findById(id)
                .orElseThrow(() -> new InvalidDataException("El socio no existe."));
        partner.setStatus(Status.INACTIVO);
        partnerDAO.update(partner);
    }

    /**
     * Busca un socio por su identificador.
     *
     * @param id identificador del socio
     * @return el socio, o vacío si no existe
     */
    public Optional<Partner> findById(int id) {
        return partnerDAO.findById(id);
    }

    /**
     * Busca un socio por su NIT.
     *
     * @param taxId NIT del socio
     * @return el socio, o vacío si no existe
     */
    public Optional<Partner> findByTaxId(String taxId) {
        return partnerDAO.findByTaxId(taxId);
    }

    /**
     * Devuelve todos los socios.
     *
     * @return lista de socios
     */
    public List<Partner> findAll() {
        return partnerDAO.findAll();
    }

    /**
     * Devuelve todos los proveedores.
     *
     * @return lista de proveedores
     */
    public List<Supplier> findAllSuppliers() {
        return partnerDAO.findAll().stream()
                .filter(Supplier.class::isInstance)
                .map(Supplier.class::cast)
                .toList();
    }

    /**
     * Devuelve todos los talleres.
     *
     * @return lista de talleres
     */
    public List<Workshop> findAllWorkshops() {
        return partnerDAO.findAll().stream()
                .filter(Workshop.class::isInstance)
                .map(Workshop.class::cast)
                .toList();
    }

    private void validate(Partner partner) {
        if (partner == null) {
            throw new InvalidDataException("El socio es obligatorio.");
        }
        partner.setCompanyName(cleanText(partner.getCompanyName(), "El nombre de la compañía"));
        partner.setTaxId(cleanText(partner.getTaxId(), "El NIT"));
        partner.setPhone(cleanText(partner.getPhone(), "El teléfono"));
        if (partner.getStatus() == null) {
            throw new InvalidDataException("El estado es obligatorio.");
        }
        if (partner.getRegistrationDate() == null) {
            throw new InvalidDataException("La fecha de registro es obligatoria.");
        }
        if (partner instanceof Supplier supplier) {
            if (supplier.getDeliveryDays() < 0) {
                throw new InvalidDataException("Los días de entrega no pueden ser negativos.");
            }
        } else if (partner instanceof Workshop workshop) {
            workshop.setSpecialty(cleanText(workshop.getSpecialty(), "La especialidad"));
            if (workshop.getBayCount() < 0) {
                throw new InvalidDataException("El número de bahías no puede ser negativo.");
            }
        } else {
            throw new InvalidDataException("Tipo de socio no soportado.");
        }
    }

    private String cleanText(String value, String field) {
        if (value == null || value.isBlank()) {
            String ending = field.startsWith("La ") ? " es obligatoria." : " es obligatorio.";
            throw new InvalidDataException(field + ending);
        }
        if (value.contains("|") || value.contains("\n") || value.contains("\r")) {
            throw new InvalidDataException(field + " no puede contener '|' ni saltos de línea.");
        }
        return value.trim();
    }
}