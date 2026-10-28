package autoparts.model;

import autoparts.util.FileUtil;

/**
 * Representa un componente de motor de un vehículo, conteniendo información sobre su cilindraje y potencia.
 * 
 * @author ALVARO
 */
public class Engine extends Component {

    private int displacementCc;
    private int cylinders;
    private FuelType fuel;
    private int horsepower;

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
     * @param displacementCc el cilindraje del motor en centímetros cúbicos
     * @param cylinders el número de cilindros del motor
     * @param fuel el tipo de combustible del motor
     * @param horsepower la potencia del motor
     */
    public Engine(ComponentType type, int id, String name, String reference, Brand brand, Category category, String origin, String status, int displacementCc, int cylinders, FuelType fuel, int horsepower) {
        super(type, id, name, reference, brand, category, origin, status);
        this.displacementCc = displacementCc;
        this.cylinders = cylinders;
        this.fuel = fuel;
        this.horsepower = horsepower;
    }

    /**
     * Convierte el objeto en una línea de registro.
     * @return Devuelve la línea para guardar en components.csv.
     */
    @Override
    public String toLine() {
        return FileUtil.join(getType().getFileValue(), getId(), getName(), getReference(), getBrand().getId(),
                getCategory().getId(), getOrigin(), getStatus(), displacementCc, cylinders, fuel.getFileValue(), horsepower);
    }

    public int getDisplacementCc() {
        return displacementCc;
    }

    public void setDisplacementCc(int displacementCc) {
        this.displacementCc = displacementCc;
    }

    public int getCylinders() {
        return cylinders;
    }

    public void setCylinders(int cylinders) {
        this.cylinders = cylinders;
    }

    public FuelType getFuel() {
        return fuel;
    }

    public void setFuel(FuelType fuel) {
        this.fuel = fuel;
    }

    public int getHorsepower() {
        return horsepower;
    }

    public void setHorsepower(int horsepower) {
        this.horsepower = horsepower;
    }
}