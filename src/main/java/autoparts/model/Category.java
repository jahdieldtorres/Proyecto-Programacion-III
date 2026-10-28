package autoparts.model;

import autoparts.util.FileUtil;

/**
 * Representa la categoría a la que pertenece un componente o autoparte.
 * 
 * @author ALVARO
 */
public class Category {

    private int id;
    private String name;

    public Category() {
    }

    /**
     * Crea una nueva instancia de Category con los valores especificados.
     * 
     * @param id el identificador único de la categoría
     * @param name el nombre de la categoría
     */
    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Convierte el objeto en una línea de registro.
     * @return Devuelve la línea para guardar en categories.csv.
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
