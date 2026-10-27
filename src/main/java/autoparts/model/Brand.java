package autoparts.model;

/**
 * Clase que describe las marcas propietarias de las autopartes y los automóviles disponibles.
 * 
 * @author alvaro
 */
public class Brand {
    private int id;
    private String name;

    public Brand(){}
    
    
    /**
     * Crea la clase de marca parametrizada con un nuombre e identificador propios.
     *
     * @param id        Identificador único de marca
     * @param name      Nombre de la marca
     */
    public Brand(int id, String name) {
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