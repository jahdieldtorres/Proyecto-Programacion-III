package autoparts.dao.file;

import autoparts.dao.InventoryDAO;
import autoparts.exception.PersistenceException;
import autoparts.model.Inventory;
import autoparts.util.FileUtil;
import autoparts.util.FlatFiles;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de {@link InventoryDAO} con el archivo {@code inventory.csv}.
 *
 * @author Héctor Guevara
 */
public class InventoryFileDAO implements InventoryDAO {

    private static final String PATH = FlatFiles.INVENTORY;

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
    public List<Inventory> findByComponent(int componentId) {
        return findAll().stream().filter(i -> i.getComponentId() == componentId).toList();
    }
}