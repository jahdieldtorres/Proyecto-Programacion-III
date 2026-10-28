package autoparts.model;

import autoparts.util.FileUtil;

/**
 * Representa un componente de motor de un vehículo, conteniendo información sobre su cilindraje y potencia.
 * 
 * @author ALVARO
 */
public class Engine extends Component {

    private int displacement;
    private int horsepower;

    public Engine() {
    }

    /**
     * Crea una nueva instancia de Engine con los valores especificados.
     * 
     * @param type tipo del componente
     * @param id identificador de producto componente
     * @param name nombre descriptivo del componente
     * @param reference referencia de fabricante
     * @param brand marca del componente
     * @param category categoría del componente
     * @param origin origen del componente
     * @param status estado cualitativo del componente
     * @param displacement el cilindraje del motor
     * @param horsepower la potencia del motor
     */
    public Engine(ComponentType type, int id, String name, String reference, Brand brand, Category category, String origin, String status, int displacement, int horsepower) {
        super(type, id, name, reference, brand, category, origin, status);
        this.displacement = displacement;
        this.horsepower = horsepower;
    }

    /**
     * Convierte el objeto en una línea de registro.
     * @return Devuelve la línea para guardar en components.csv.
     */
    @Override
    public String toLine() {
        return FileUtil.join(getType().getFileValue(), getId(), getName(), getReference(), getBrand().getId(),
                getCategory().getId(), getOrigin(), getStatus(), displacement, horsepower);
    }

    public int getDisplacement() {
        return displacement;
    }

    public void setDisplacement(int displacement) {
        this.displacement = displacement;
    }

    public int getHorsepower() {
        return horsepower;
    }

    public void setHorsepower(int horsepower) {
        this.horsepower = horsepower;
    }
}