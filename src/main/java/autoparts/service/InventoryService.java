package autoparts.service;

import autoparts.dao.InventoryDAO;
import autoparts.dao.PartnerDAO;
import autoparts.exception.InvalidDataException;
import autoparts.model.Inventory;
import autoparts.model.Partner;
import autoparts.model.Status;
import autoparts.model.Supplier;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Reglas de negocio del inventario de los proveedores: validación de los datos
 * (precio mayor que cero, stock no negativo, proveedor activo), una sola oferta
 * activa por proveedor y componente, borrado lógico y el precio más bajo de un
 * componente. Solo depende de las interfaces {@link InventoryDAO} y {@link PartnerDAO}.
 *
 * @author Héctor Guevara
 */
public class InventoryService {

    private final InventoryDAO inventoryDAO;
    private final PartnerDAO partnerDAO;

    /**
     * Crea el servicio con sus DAO.
     *
     * @param inventoryDAO acceso a datos del inventario
     * @param partnerDAO   acceso a datos de los socios, para revisar al proveedor de cada oferta
     */
    public InventoryService(InventoryDAO inventoryDAO, PartnerDAO partnerDAO) {
        this.inventoryDAO = inventoryDAO;
        this.partnerDAO = partnerDAO;
    }

    /**
     * Registra una oferta nueva. Le asigna el siguiente identificador libre.
     *
     * @param item oferta a registrar
     * @throws InvalidDataException si algún dato no es válido
     */
    public void register(Inventory item) {
        validate(item);
        item.setId(inventoryDAO.nextId());
        inventoryDAO.save(item);
    }

    /**
     * Actualiza una oferta. No se puede cambiar su proveedor ni su componente.
     *
     * @param item oferta con los datos nuevos
     * @throws InvalidDataException si la oferta no existe, cambia de proveedor o
     *                              de componente, o algún dato no es válido
     */
    public void update(Inventory item) {
        if (item == null) {
            throw new InvalidDataException("La oferta es obligatoria.");
        }
        Inventory existing = inventoryDAO.findById(item.getId())
                .orElseThrow(() -> new InvalidDataException("La oferta no existe."));
        if (existing.getSupplierId() != item.getSupplierId()
                || existing.getComponentId() != item.getComponentId()) {
            throw new InvalidDataException("No se puede cambiar el proveedor ni el componente de una oferta.");
        }
        validate(item);
        inventoryDAO.update(item);
    }

    /**
     * Elimina una oferta con borrado lógico: pasa a estado {@code INACTIVO}.
     *
     * @param id identificador de la oferta
     * @throws InvalidDataException si la oferta no existe
     */
    public void delete(int id) {
        Inventory item = inventoryDAO.findById(id)
                .orElseThrow(() -> new InvalidDataException("La oferta no existe."));
        item.setStatus(Status.INACTIVO);
        inventoryDAO.update(item);
    }

    /**
     * Busca una oferta por su identificador.
     *
     * @param id identificador de la oferta
     * @return la oferta, o vacío si no existe
     */
    public Optional<Inventory> findById(int id) {
        return inventoryDAO.findById(id);
    }

    /**
     * Devuelve todas las ofertas, activas o no.
     *
     * @return lista de ofertas
     */
    public List<Inventory> findAll() {
        return inventoryDAO.findAll();
    }

    /**
     * Devuelve las ofertas de un proveedor, activas o no.
     *
     * @param supplierId identificador del proveedor
     * @return lista de ofertas del proveedor
     * @throws InvalidDataException si falta el proveedor
     */
    public List<Inventory> findBySupplier(int supplierId) {
        if (supplierId <= 0) {
            throw new InvalidDataException("El proveedor es obligatorio.");
        }
        return inventoryDAO.findBySupplier(supplierId);
    }

    /**
     * Devuelve el precio más bajo de un componente (sin contar el envío). Solo
     * cuentan las ofertas activas, con stock y de un proveedor activo, las mismas
     * que usa el comparador. Las tarjetas del catálogo muestran "desde $X" con este valor.
     *
     * @param componentId identificador del componente
     * @return el precio más bajo en pesos, o vacío si ningún proveedor lo ofrece
     * @throws InvalidDataException si falta el componente
     */
    public OptionalInt findLowestPrice(int componentId) {
        if (componentId <= 0) {
            throw new InvalidDataException("El componente es obligatorio.");
        }
        return inventoryDAO.findByComponent(componentId).stream()
                .filter(item -> item.isActive() && item.getStock() > 0 && isActiveSupplier(item.getSupplierId()))
                .mapToInt(Inventory::getPrice)
                .min();
    }

    private void validate(Inventory item) {
        if (item == null) {
            throw new InvalidDataException("La oferta es obligatoria.");
        }
        if (item.getComponentId() <= 0) {
            throw new InvalidDataException("El componente es obligatorio.");
        }
        if (item.getSupplierId() <= 0) {
            throw new InvalidDataException("El proveedor es obligatorio.");
        }
        Optional<Partner> partner = partnerDAO.findById(item.getSupplierId());
        if (partner.isEmpty() || !(partner.get() instanceof Supplier)) {
            throw new InvalidDataException("El proveedor no existe.");
        }
        if (partner.get().getStatus() != Status.ACTIVO) {
            throw new InvalidDataException("El proveedor debe estar activo.");
        }
        if (item.getPrice() <= 0) {
            throw new InvalidDataException("El precio debe ser mayor que cero.");
        }
        if (item.getStock() < 0) {
            throw new InvalidDataException("El stock no puede ser negativo.");
        }
        if (item.getShippingCost() < 0) {
            throw new InvalidDataException("El costo de envío no puede ser negativo.");
        }
        if (item.getWarrantyMonths() < 0) {
            throw new InvalidDataException("La garantía no puede ser negativa.");
        }
        if (item.getCondition() == null) {
            throw new InvalidDataException("La condición es obligatoria.");
        }
        if (item.getStatus() == null) {
            throw new InvalidDataException("El estado es obligatorio.");
        }
        if (item.isActive()) {
            checkNoDuplicate(item);
        }
    }

    /**
     * Un proveedor solo puede tener una oferta activa por componente.
     */
    private void checkNoDuplicate(Inventory item) {
        for (Inventory other : inventoryDAO.findBySupplier(item.getSupplierId())) {
            if (other.getId() != item.getId() && other.isActive()
                    && other.getComponentId() == item.getComponentId()) {
                throw new InvalidDataException("Ese proveedor ya tiene una oferta activa de este componente.");
            }
        }
    }

    private boolean isActiveSupplier(int supplierId) {
        Optional<Partner> partner = partnerDAO.findById(supplierId);
        return partner.isPresent() && partner.get() instanceof Supplier
                && partner.get().getStatus() == Status.ACTIVO;
    }
}