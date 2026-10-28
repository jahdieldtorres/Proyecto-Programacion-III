package autoparts.service;

import autoparts.dao.FeeDAO;
import autoparts.model.Fee;
import autoparts.model.Status;

import java.util.List;
import java.util.Optional;


public class FeeService {

    private final FeeDAO feeDAO;

   
    public FeeService(FeeDAO feeDAO) {
        this.feeDAO = feeDAO;
    }


    public void register(Fee fee) {
        validate(fee);
        fee.setId(feeDAO.nextId());
        if (fee.getStatus() == Status.ACTIVO) {
            deactivateOthers(fee.getServiceId(), fee.getId());
        }
        feeDAO.save(fee);
    }

    public void update(Fee fee) {
        validate(fee);
        if (feeDAO.findById(fee.getId()).isEmpty()) {
            throw new IllegalArgumentException("La tarifa no existe.");
        }
        if (fee.getStatus() == Status.ACTIVO) {
            deactivateOthers(fee.getServiceId(), fee.getId());
        }
        feeDAO.update(fee);
    }

    public void delete(int id) {
        Fee fee = feeDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La tarifa no existe."));
        fee.setStatus(Status.INACTIVO);
        feeDAO.update(fee);
    }


    public Optional<Fee> findById(int id) {
        return feeDAO.findById(id);
    }


    public List<Fee> findByService(int serviceId) {
        return feeDAO.findByService(serviceId);
    }

 
    public Optional<Fee> findCurrentByService(int serviceId) {
        return feeDAO.findByService(serviceId).stream()
                .filter(f -> f.getStatus() == Status.ACTIVO)
                .findFirst();
    }


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
            throw new IllegalArgumentException("La tarifa es obligatoria.");
        }
        if (fee.getServiceId() <= 0) {
            throw new IllegalArgumentException("El servicio es obligatorio.");
        }
        if (fee.getAmount() <= 0) {
            throw new IllegalArgumentException("El valor de la tarifa debe ser mayor que cero.");
        }
        if (fee.getStartDate() == null) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria.");
        }
        if (fee.getStatus() == null) {
            throw new IllegalArgumentException("El estado es obligatorio.");
        }
    }
}