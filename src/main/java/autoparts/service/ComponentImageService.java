package autoparts.service;

import autoparts.dao.ComponentImageDAO;
import autoparts.model.ComponentImage;
import autoparts.util.ImageStorage;
import java.util.HashMap;
import java.util.Map;

/**
 * Entrega la foto principal de cada repuesto.
 *
 * Las fotos casi nunca cambian, así que cada una se busca en la base de datos
 * una sola vez y después se toma de una memoria (caché). Eso hace que el
 * catálogo cargue rápido la segunda vez.
 *
 * @author Jahdiel Torres
 */
public class ComponentImageService {

    private final ComponentImageDAO imageDAO;
    /** Foto ya buscada de cada repuesto: id del componente → dirección (o null si no tiene). */
    private final Map<Integer, String> cache = new HashMap<>();

    /**
     * @param imageDAO acceso a la tabla de fotos de los componentes
     */
    public ComponentImageService(ComponentImageDAO imageDAO) {
        this.imageDAO = imageDAO;
    }

    /**
     * Devuelve la dirección de la foto principal del repuesto, lista para
     * mostrarla en la pantalla.
     *
     * @param componentId id del componente
     * @return dirección del archivo (file:/...), o null si no tiene foto
     */
    public String findMainImageUrl(int componentId) {
        if (!cache.containsKey(componentId)) {
            cache.put(componentId, searchMainImage(componentId));
        }
        return cache.get(componentId);
    }

    /**
     * Busca la foto en la base de datos y revisa que el archivo exista en la
     * carpeta de imágenes.
     */
    private String searchMainImage(int componentId) {
        ComponentImage image = imageDAO.findMain(componentId);
        if (image == null || !ImageStorage.exists(image.getFileName())) {
            return null;
        }
        return ImageStorage.resolve(image.getFileName()).toUri().toString();
    }
}
