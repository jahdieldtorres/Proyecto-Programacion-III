package autoparts.dao.file;

import autoparts.dao.InventoryDAO;
import autoparts.exception.PersistenceException;
import autoparts.model.Inventory;
import autoparts.util.FileUtil;
import autoparts.util.FlatFiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de {@link InventoryDAO} con el archivo {@code inventory.csv}:
 * una línea por existencia. Es la única clase que toca ese archivo.
 *
 * Cuando se pase a base de datos, esta clase se reemplaza por otra que
 * implemente la misma interfaz y los servicios no cambian.
 *
 * @author Héctor Guevara
 */
public class InventoryFileDAO implements InventoryDAO {

    private static final String PATH = FlatFiles.INVENTORY;

    @Override
    public int nextId() {
        return FileUtil.nextId(PATH, 0);
    }

    @Override
    public void save(Inventory item) {
        FileUtil.append(PATH, item.toLine());
    }

    @Override
    public boolean update(Inventory item) {
        List<Inventory> items = findAll();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == item.getId()) {
                items.set(i, item);
                rewriteAll(items);
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<Inventory> findById(int id) {
        return findAll().stream().filter(i -> i.getId() == id).findFirst();
    }

    @Override
    public List<Inventory> findAll() {
        List<Inventory> items = new ArrayList<>();
        for (String line : FileUtil.read(PATH)) {
            try {
                items.add(new Inventory(line));
            } catch (RuntimeException e) {
                throw new PersistenceException("Registro inválido en " + PATH + ": " + line, e);
            }
        }
        return items;
    }

    @Override
    public List<Inventory> findBySupplier(int supplierId) {
        return findAll().stream().filter(i -> i.getSupplierId() == supplierId).toList();
    }

    @Override
    public List<Inventory> findByComponent(int componentId) {
        return findAll().stream().filter(i -> i.getComponentId() == componentId).toList();
    }

    private void rewriteAll(List<Inventory> items) {
        List<String> lines = new ArrayList<>();
        for (Inventory item : items) {
            lines.add(item.toLine());
        }
        FileUtil.rewrite(PATH, lines);
    }
}