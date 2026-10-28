package autoparts.service;

import autoparts.dao.InventoryDAO;
import autoparts.dao.PartnerDAO;
import autoparts.exception.InvalidDataException;
import autoparts.model.ComparisonCriterion;
import autoparts.model.Inventory;
import autoparts.model.Offer;
import autoparts.model.Partner;
import autoparts.model.Status;
import autoparts.model.Supplier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Comparador de ofertas: dado un componente y un criterio, devuelve las
 * ofertas de los proveedores ordenadas de la mejor a la peor. Solo cuentan las
 * ofertas activas, con stock y de un proveedor activo.
 *
 * <p>Recibe los DAO por constructor y depende solo de sus interfaces.</p>
 *
 * @author Héctor Guevara
 */
public class ComparatorService {

    private final InventoryDAO inventoryDAO;
    private final PartnerDAO partnerDAO;

    /**
     * Crea el servicio con sus DAO.
     *
     * @param inventoryDAO acceso a datos del inventario
     * @param partnerDAO   acceso a datos de los socios, para conocer al proveedor de cada oferta
     */
    public ComparatorService(InventoryDAO inventoryDAO, PartnerDAO partnerDAO) {
        this.inventoryDAO = inventoryDAO;
        this.partnerDAO = partnerDAO;
    }

    /**
     * Compara las ofertas de un componente.
     *
     * @param componentId identificador del componente
     * @param criterion   criterio con el que se ordenan las ofertas
     * @return ofertas ordenadas con la mejor primera; lista vacía si ningún
     *         proveedor activo tiene stock
     * @throws InvalidDataException si falta el componente o el criterio
     */
    public List<Offer> compare(int componentId, ComparisonCriterion criterion) {
        if (componentId <= 0) {
            throw new InvalidDataException("Selecciona un componente.");
        }
        if (criterion == null) {
            throw new InvalidDataException("Selecciona un criterio de comparación.");
        }

        List<Offer> offers = new ArrayList<>();
        for (Inventory item : inventoryDAO.findByComponent(componentId)) {
            if (!item.isActive() || item.getStock() <= 0) {
                continue;
            }
            Optional<Partner> partner = partnerDAO.findById(item.getSupplierId());
            if (partner.isPresent() && partner.get() instanceof Supplier supplier
                    && supplier.getStatus() == Status.ACTIVO) {
                offers.add(new Offer(supplier, item));
            }
        }
        offers.sort(criterion.getComparator());
        return offers;
    }
}