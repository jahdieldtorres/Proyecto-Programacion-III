package autoparts.service;

import autoparts.dao.FeeDAO;
import autoparts.exception.InvalidDataException;
import autoparts.model.Fee;
import autoparts.model.Status;

import java.util.List;
import java.util.Optional;

/**
 * Reglas de negocio de las tarifas de los servicios de los talleres. Un
 * servicio tiene como máximo una tarifa vigente ({@code ACTIVO}): al registrar
 * una tarifa activa nueva, la anterior pasa a {@code INACTIVO}.
 *
 * @author Héctor Guevara
 */
public class FeeService {

    private final FeeDAO feeDAO;

    /**
     * Crea el servicio con su DAO.
     *
     * @param feeDAO acceso a datos de las tarifas
     */
    public FeeService(FeeDAO feeDAO) {
        this.feeDAO = feeDAO;
    }

    /**
     * Registra una tarifa nueva. Si queda activa, desactiva la que estaba vigente.
     *
     * @param fee tarifa a registrar
     * @throws InvalidDataException si algún dato no es válido
     */
    public void register(Fee fee) {
        validate(fee);
        fee.setId(feeDAO.nextId());
        if (fee.getStatus() == Status.ACTIVO) {
            deactivateOthers(fee.getServiceId(), fee.getId());
        }
        feeDAO.save(fee);
    }

    /**
     * Actualiza una tarifa. Si queda activa, desactiva la otra vigente del servicio.
     *
     * @param fee tarifa con los datos nuevos
     * @throws InvalidDataException si la tarifa no existe o algún dato no es válido
     */
    public void update(Fee fee) {
        validate(fee);
        if (feeDAO.findById(fee.getId()).isEmpty()) {
            throw new InvalidDataException("La tarifa no existe.");
        }
        if (fee.getStatus() == Status.ACTIVO) {
            deactivateOthers(fee.getServiceId(), fee.getId());
        }
        feeDAO.update(fee);
    }

    /**
     * Elimina una tarifa con borrado lógico: pasa a estado {@code INACTIVO}.
     *
     * @param id identificador de la tarifa
     * @throws InvalidDataException si la tarifa no existe
     */
    public void delete(int id) {
        Fee fee = feeDAO.findById(id)
                .orElseThrow(() -> new InvalidDataException("La tarifa no existe."));
        fee.setStatus(Status.INACTIVO);
        feeDAO.update(fee);
    }

    /**
     * Busca una tarifa por su identificador.
     *
     * @param id identificador de la tarifa
     * @return la tarifa, o vacío si no existe
     */
    public Optional<Fee> findById(int id) {
        return feeDAO.findById(id);
    }

    /**
     * Devuelve todas las tarifas de un servicio, vigentes o no.
     *
     * @param serviceId identificador del servicio
     * @return lista de tarifas del servicio
     */
    public List<Fee> findByService(int serviceId) {
        return feeDAO.findByService(serviceId);
    }

    /**
     * Devuelve la tarifa vigente de un servicio.
     *
     * @param serviceId identificador del servicio
     * @return la tarifa {@code ACTIVO}, o vacío si no tiene
     */
    public Optional<Fee> findCurrentByService(int serviceId) {
        return feeDAO.findByService(serviceId).stream()
                .filter(f -> f.getStatus() == Status.ACTIVO)
                .findFirst();
    }

    /**
     * Devuelve todas las tarifas.
     *
     * @return lista de tarifas
     */
    public List<Fee> findAll() {
        return feeDAO.findAll();
    }

    private void deactivateOthers(int serviceId, int keepId) {
        for (Fee other : feeDAO.findByService(serviceId)) {
            if (other.getId() != keepId && other.getStatus() == Status.ACTIVO) {
                other.setStatus(Status.INACTIVO);
                feeDAO.update(other);
            }
        }
    }

    private void validate(Fee fee) {
        if (fee == null) {
            throw new InvalidDataException("La tarifa es obligatoria.");
        }
        if (fee.getServiceId() <= 0) {
            throw new InvalidDataException("El servicio es obligatorio.");
        }
        if (fee.getAmount() <= 0) {
            throw new InvalidDataException("El valor de la tarifa debe ser mayor que cero.");
        }
        if (fee.getStartDate() == null) {
            throw new InvalidDataException("La fecha de inicio es obligatoria.");
        }
        if (fee.getStatus() == null) {
            throw new InvalidDataException("El estado es obligatorio.");
        }
    }
}