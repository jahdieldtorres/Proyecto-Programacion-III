package autoparts.model;

/**
 *
 * @author ALVARO
 */
public abstract class Component {
    
    private int id;
    private String name;
    private String description;
    private String status;

    public Component() {
    }

    /**
     * Crea la clase de componentes con un nombre descriptivo e identificador único de postulamiento.
     *
     * @param id            Identificador de producto componente
     * @param name          Nombre descriptivo del componente
     * @param description   Descripción del componente
     * @param status        Estado cualitativo del componente
     */
    public Component(int id, String name, String description, String status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
