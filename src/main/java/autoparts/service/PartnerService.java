package autoparts.service;

import autoparts.dao.PartnerDAO;
import autoparts.exception.InvalidDataException;
import autoparts.model.Partner;
import autoparts.model.Status;
import autoparts.model.Supplier;
import autoparts.model.Workshop;
import autoparts.util.ImageStorage;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Reglas de negocio de los socios (proveedores y talleres): validación de los
 * datos (incluida su ubicación y su descripción), NIT único, foto del socio y borrado lógico. Solo depende de la interfaz {@link PartnerDAO}.
 *
 * @author Héctor Guevara
 */
public class PartnerService {

    /** Máximo de caracteres de la descripción de un socio (VARCHAR2(300) en Oracle). */
    public static final int MAX_DESCRIPTION_LENGTH = 300;

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
     * Cambia la foto de un socio ya guardado. Copia el archivo a
     * {@code images/partners/}, guarda su nombre en el socio y borra la foto
     * anterior (si tenía).
     *
     * @param partnerId identificador del socio
     * @param photo     archivo de la foto elegida (.jpg, .jpeg o .png)
     * @throws InvalidDataException si el socio no existe o el archivo no es una foto válida
     */
    public void changePhoto(int partnerId, File photo) {
        if (!ImageStorage.isSupported(photo)) {
            throw new InvalidDataException("La foto debe ser un archivo .jpg o .png que exista.");
        }
        Partner partner = partnerDAO.findById(partnerId)
                .orElseThrow(() -> new InvalidDataException("El socio no existe."));
        String oldFile = partner.getImageFile();
        partner.setImageFile(ImageStorage.store(photo, ImageStorage.PARTNERS_DIR));
        try {
            partnerDAO.update(partner);
        } catch (RuntimeException e) {
            ImageStorage.delete(ImageStorage.PARTNERS_DIR, partner.getImageFile());
            throw e;
        }
        if (oldFile != null && !oldFile.equals(partner.getImageFile())) {
            try {
                ImageStorage.delete(ImageStorage.PARTNERS_DIR, oldFile);
            } catch (RuntimeException ignored) {
                // La foto nueva ya quedó guardada; un archivo viejo que sobra no es grave.
            }
        }
    }

    /**
     * Devuelve la ruta de la foto de un socio, lista para mostrarla en pantalla.
     *
     * @param partner socio del que se quiere la foto
     * @return dirección del archivo (file:/...), o null si no tiene foto o el archivo no existe
     */
    public String findPhotoUrl(Partner partner) {
        if (partner == null || !ImageStorage.exists(ImageStorage.PARTNERS_DIR, partner.getImageFile())) {
            return null;
        }
        return ImageStorage.resolve(ImageStorage.PARTNERS_DIR, partner.getImageFile())
                .toAbsolutePath().toUri().toString();
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
        partner.setAddress(cleanText(partner.getAddress(), "La dirección"));
        if (Double.isNaN(partner.getLatitude()) || partner.getLatitude() < -90 || partner.getLatitude() > 90) {
            throw new InvalidDataException("La latitud debe estar entre -90 y 90.");
        }
        if (Double.isNaN(partner.getLongitude()) || partner.getLongitude() < -180 || partner.getLongitude() > 180) {
            throw new InvalidDataException("La longitud debe estar entre -180 y 180.");
        }
        validateDescription(partner);
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

    /**
     * La descripción es opcional: si viene vacía queda en null; si no, se recorta
     * y no puede pasar de {@link #MAX_DESCRIPTION_LENGTH} caracteres.
     */
    private void validateDescription(Partner partner) {
        String description = partner.getDescription();
        if (description == null || description.isBlank()) {
            partner.setDescription(null);
            return;
        }
        description = description.trim();
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidDataException("La descripción no puede pasar de " + MAX_DESCRIPTION_LENGTH
                    + " caracteres (tiene " + description.length() + ").");
        }
        partner.setDescription(description);
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