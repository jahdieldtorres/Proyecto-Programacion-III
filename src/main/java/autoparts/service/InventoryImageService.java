package autoparts.service;

import autoparts.dao.InventoryImageDAO;
import autoparts.exception.InvalidDataException;
import autoparts.model.InventoryImage;
import autoparts.util.ImageStorage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Maneja la foto principal de cada oferta de inventario.
 *
 * Cada tienda sube la foto de su oferta (el estado real del repuesto), que es
 * distinta de la foto general del catálogo. Las fotos se buscan en la base una
 * sola vez y después se toman de una memoria (caché), como hace
 * {@link ComponentImageService}. La memoria se actualiza cuando se cambia la
 * foto de una oferta.
 *
 * Las fotos se guardan en {@link ImageStorage#INVENTORY_DIR}; en la base solo
 * queda el nombre del archivo.
 *
 * @author Álvaro Aramendiz
 */
public class InventoryImageService {

    private final InventoryImageDAO imageDAO;
    /** Foto ya buscada de cada oferta: id de la oferta → dirección (o null si no tiene). */
    private final Map<Integer, String> cache = new HashMap<>();

    /**
     * Crea el servicio con su DAO.
     *
     * @param imageDAO acceso a la tabla de fotos de las ofertas
     */
    public InventoryImageService(InventoryImageDAO imageDAO) {
        this.imageDAO = imageDAO;
    }

    /**
     * Devuelve la dirección de la foto principal de la oferta, lista para
     * mostrarla en la pantalla.
     *
     * @param inventoryId identificador de la oferta
     * @return dirección del archivo (file:/...), o null si la oferta no tiene foto
     */
    public String findMainImageUrl(int inventoryId) {
        if (!cache.containsKey(inventoryId)) {
            cache.put(inventoryId, searchMainImage(inventoryId));
        }
        return cache.get(inventoryId);
    }

    /**
     * Guarda la foto principal de una oferta. Copia el archivo a
     * {@code images/inventory/}; si la oferta ya tenía foto principal, cambia
     * el registro por la nueva y borra el archivo anterior.
     *
     * @param inventoryId identificador de la oferta (debe estar guardada)
     * @param photo archivo de la foto elegida (.jpg, .jpeg o .png)
     * @throws InvalidDataException si falta la oferta o el archivo no es una foto válida
     */
    public void saveMainImage(int inventoryId, File photo) {
        if (inventoryId <= 0) {
            throw new InvalidDataException("La oferta debe estar guardada antes de agregarle una foto.");
        }
        if (!ImageStorage.isSupported(photo)) {
            throw new InvalidDataException("La foto debe ser un archivo .jpg o .png que exista.");
        }
        String fileName = ImageStorage.store(photo, ImageStorage.INVENTORY_DIR);
        InventoryImage current = imageDAO.findMain(inventoryId);
        if (current == null) {
            imageDAO.save(new InventoryImage(0, inventoryId, fileName, null, true, 0));
        } else {
            String oldFileName = current.getFileName();
            current.setFileName(fileName);
            current.setMain(true);
            imageDAO.update(current);
            deleteQuietly(oldFileName);
        }
        cache.remove(inventoryId);
    }

    /**
     * Busca la foto en la base de datos y revisa que el archivo exista en la
     * carpeta de imágenes de inventario.
     */
    private String searchMainImage(int inventoryId) {
        InventoryImage image = imageDAO.findMain(inventoryId);
        if (image == null || !ImageStorage.exists(ImageStorage.INVENTORY_DIR, image.getFileName())) {
            return null;
        }
        return ImageStorage.resolve(ImageStorage.INVENTORY_DIR, image.getFileName()).toUri().toString();
    }

    /**
     * Borra un archivo viejo del disco. Si no se puede, no importa: la foto
     * nueva ya quedó guardada y solo sobra un archivo.
     */
    private void deleteQuietly(String fileName) {
        try {
            ImageStorage.delete(ImageStorage.INVENTORY_DIR, fileName);
        } catch (RuntimeException ignored) {
            // El archivo viejo queda en la carpeta; no afecta a la oferta.
        }
    }
}
