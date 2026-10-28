package autoparts.model;

import autoparts.util.FileUtil;

/**
 * Representa el componente de llanta de un vehículo, detallando sus medidas e índices.
 * 
 * @author ALVARO
 */
public class Tire extends Component {

    private double width;
    private double profile;
    private double diameter;
    private double load;
    private double velocityIndex;

    public Tire() {
    }

    /**
     * Crea una nueva instancia de Tire con los valores especificados.
     *
     * @param type tipo del componente
     * @param id identificador de producto componente
     * @param name nombre descriptivo del componente
     * @param reference referencia de fabricante
     * @param brand marca del componente
     * @param category categoría del componente
     * @param origin origen del componente
     * @param status estado cualitativo del componente
     * @param width ancho de la llanta
     * @param profile perfil de la llanta
     * @param diameter diámetro de la llanta
     * @param load carga de la llanta
     * @param velocityIndex índice de velocidad de la llanta
     */
    public Tire(ComponentType type, int id, String name, String reference, Brand brand, Category category, String origin, String status, double width, double profile, double diameter, double load, double velocityIndex) {
        super(type, id, name, reference, brand, category, origin, status);
        this.width = width;
        this.profile = profile;
        this.diameter = diameter;
        this.load = load;
        this.velocityIndex = velocityIndex;
    }

    /**
     * Convierte el objeto en una línea de registro.
     * @return Devuelve la línea para guardar en components.csv.
     */
    @Override
    public String toLine() {
        return FileUtil.join(getType().getFileValue(), getId(), getName(), getReference(), getBrand().getId(),
                getCategory().getId(), getOrigin(), getStatus(), width, profile, diameter, load, velocityIndex);
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getProfile() {
        return profile;
    }

    public void setProfile(double profile) {
        this.profile = profile;
    }

    public double getDiameter() {
        return diameter;
    }

    public void setDiameter(double diameter) {
        this.diameter = diameter;
    }

    public double getLoad() {
        return load;
    }

    public void setLoad(double load) {
        this.load = load;
    }

    public double getVelocityIndex() {
        return velocityIndex;
    }

    public void setVelocityIndex(double velocityIndex) {
        this.velocityIndex = velocityIndex;
    }
}