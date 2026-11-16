package autoparts.model;

/**
 * Describe una foto de un componente (repuesto).
 *
 * En la base solo se guarda el nombre del archivo; la foto en sí está en la
 * carpeta images/components/ (ver {@link autoparts.util.ImageStorage}). Un
 * componente puede tener varias fotos y una de ellas es la principal, la que
 * se muestra en las tarjetas del catálogo.
 *
 * @author ALVARO
 */
public class ComponentImage {

    private int id;
    private int componentId;
    private String fileName;
    private boolean main;
    private int sortOrder;

    /**
     * Crea una foto de componente.
     *
     * @param id identificador de la foto (0 si todavía no se guarda)
     * @param componentId identificador del componente al que pertenece
     * @param fileName nombre del archivo dentro de images/components/
     * @param main true si es la foto principal del componente
     * @param sortOrder posición entre las fotos del componente (la menor va primero)
     */
    public ComponentImage(int id, int componentId, String fileName, boolean main, int sortOrder) {
        this.id = id;
        this.componentId = componentId;
        this.fileName = fileName;
        this.main = main;
        this.sortOrder = sortOrder;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getComponentId() {
        return componentId;
    }

    public void setComponentId(int componentId) {
        this.componentId = componentId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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
