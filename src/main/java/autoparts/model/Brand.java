package autoparts.model;

import autoparts.util.FileUtil;

/**
 * Clase que describe las marcas propietarias de las autopartes y los automóviles disponibles.
 * 
 * @author ALVARO
 */
public class Brand {
    private int id;
    private String name;

    public Brand(){}
    
    /**
     * Crea la clase de marca parametrizada con un nombre e identificador propios.
     *
     * @param id Identificador único de marca
     * @param name Nombre de la marca
     */
    public Brand(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Convierte el objeto en una línea de registro.
     * @return Devuelve la línea para guardar en brands.csv.
     */
    public String toLine() {
        return FileUtil.join(id, name);
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