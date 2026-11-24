package autoparts.service;

import autoparts.dao.CompatibilityDAO;
import autoparts.dao.InventoryDAO;
import autoparts.model.CatalogItem;
import autoparts.model.Component;
import autoparts.model.Inventory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Arma el catálogo que ve el cliente: junta cada repuesto con su precio más
 * bajo, cuántos almacenes lo tienen, su foto y si sirve para su vehículo.
 *
 * Es una fachada: la pantalla le pide todo a este servicio en una sola
 * llamada, en vez de consultar cuatro servicios distintos. Las ofertas se
 * leen una sola vez para todos los repuestos, no una vez por repuesto.
 *
 * @author Jahdiel Torres
 */
public class CatalogService {

    private final ComponentService componentService;
    private final ComponentImageService imageService;
    private final InventoryDAO inventoryDAO;
    private final CompatibilityDAO compatibilityDAO;

    /**
     * @param componentService búsqueda de repuestos (ya filtra los activos)
     * @param imageService     fotos de los repuestos
     * @param inventoryDAO     ofertas de los almacenes
     * @param compatibilityDAO qué repuestos sirven para cada vehículo
     */
    public CatalogService(ComponentService componentService, ComponentImageService imageService,
            InventoryDAO inventoryDAO, CompatibilityDAO compatibilityDAO) {
        this.componentService = componentService;
        this.imageService = imageService;
        this.inventoryDAO = inventoryDAO;
        this.compatibilityDAO = compatibilityDAO;
    }

    /**
     * Busca repuestos activos y los ordena: primero los que tienen ofertas,
     * del más barato al más caro.
     *
     * @param text       nombre, marca o referencia (vacío = todos)
     * @param categoryId categoría, o null para todas
     * @param vehicleId  vehículo elegido, o null si no se eligió
     * @return los repuestos con su resumen de ofertas
     */
    public List<CatalogItem> search(String text, Integer categoryId, Integer vehicleId) {
        List<Component> components = componentService.search(text, categoryId);
        Map<Integer, List<Integer>> pricesByComponent = activePricesByComponent();
        Set<Integer> compatibleIds = vehicleId == null
                ? null
                : new HashSet<>(compatibilityDAO.listComponentIds(vehicleId));

        List<CatalogItem> items = new ArrayList<>();
        for (Component component : components) {
            List<Integer> prices = pricesByComponent.getOrDefault(component.getId(), List.of());
            int lowest = prices.isEmpty() ? 0 : prices.stream().min(Integer::compare).get();
            Boolean compatible = compatibleIds == null ? null : compatibleIds.contains(component.getId());
            String image = imageService.findMainImageUrl(component.getId());
            items.add(new CatalogItem(component, lowest, prices.size(), image, compatible));
        }
        items.sort(Comparator.comparing((CatalogItem item) -> !item.isAvailable())
                .thenComparingInt(CatalogItem::getLowestPrice));
        return items;
    }

    /**
     * Devuelve los repuestos para destacar en Inicio: los que más almacenes
     * ofrecen, porque ahí es donde más sirve comparar.
     *
     * @param limit cuántos devolver como máximo
     * @return los repuestos destacados
     */
    public List<CatalogItem> featured(int limit) {
        List<CatalogItem> items = new ArrayList<>(search("", null, null));
        items.removeIf(item -> !item.isAvailable() || item.getImageUrl() == null);
        items.sort(Comparator.comparingInt(CatalogItem::getStoreCount).reversed()
                .thenComparingInt(CatalogItem::getLowestPrice));
        return items.subList(0, Math.min(limit, items.size()));
    }

    /**
     * Lee todas las ofertas una vez y agrupa los precios de las que están
     * activas y con stock, por repuesto.
     */
    private Map<Integer, List<Integer>> activePricesByComponent() {
        Map<Integer, List<Integer>> prices = new HashMap<>();
        for (Inventory offer : inventoryDAO.findAll()) {
            if (offer.isActive() && offer.getStock() > 0) {
                prices.computeIfAbsent(offer.getComponentId(), id -> new ArrayList<>()).add(offer.getPrice());
            }
        }
        return prices;
    }
}
