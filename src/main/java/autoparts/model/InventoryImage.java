package autoparts.model;

/**
 * Describe una foto de una oferta de inventario (lo que una tienda vende).
 *
 * A diferencia de {@link ComponentImage}, que es la foto general del producto
 * del catálogo, esta foto es de una tienda en particular: puede mostrar el
 * estado real del repuesto o accesorios que vienen con él. En la base solo se
 * guarda el nombre del archivo, que está en la carpeta images/inventory/.
 * Una oferta puede tener varias fotos y una de ellas es la principal.
 *
 * @author Álvaro Aramendiz
 */
public class InventoryImage {

    private int id;
    private int inventoryId;
    private String fileName;
    private String caption;
    private boolean main;
    private int sortOrder;

    /**
     * Crea una foto de una oferta de inventario.
     *
     * @param id identificador de la foto (0 si todavía no se guarda)
     * @param inventoryId identificador de la oferta a la que pertenece
     * @param fileName nombre del archivo dentro de images/inventory/
     * @param caption texto corto que explica qué se ve (puede ser null)
     * @param main true si es la foto principal de la oferta
     * @param sortOrder posición entre las fotos de la oferta (la menor va primero)
     */
    public InventoryImage(int id, int inventoryId, String fileName, String caption,
            boolean main, int sortOrder) {
        this.id = id;
        this.inventoryId = inventoryId;
        this.fileName = fileName;
        this.caption = caption;
        this.main = main;
        this.sortOrder = sortOrder;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public boolean isMain() {
        return main;
    }

    public void setMain(boolean main) {
        this.main = main;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
