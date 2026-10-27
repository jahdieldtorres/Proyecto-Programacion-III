package autoparts.model;

/**
 *
 * @author ALVARO
 */
public abstract class Component {
    
    private int id;
    private String name;

    public Component() {
    }

    /**
     * Crea la clase de componentes con un nombre descriptivo e identificador único de postulamiento.
     *
     * @param id        Identificador de producto componente
     * @param name      Nombre descriptivo del componente
     */
    public Component(int id, String name) {
        this.id = id;
        this.name = name;
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
}
