package autoparts.model;

import autoparts.util.FileUtil;

/**
 * Clase que describe el componente rin de vehículos
 * @author ALVARO
 */
public class Rim extends Component {
    private double width;
    private double profile;
    private double diameter;
    private String load;
    private String material;

    public Rim() {
    }

    /**
     * Crea la clase de componentes con un nombre descriptivo e identificador
     * único de postulamiento.
     *
     * @param type tipo del componente
     * @param id identificador de producto componente
     * @param name nombre descriptivo del componente
     * @param reference referencia de fabricante
     * @param brand marca del componente
     * @param category categoría del componente
     * @param origin origen del componente
     * @param status estado cualitativo del componente
     * @param width ancho del rin
     * @param profile perfil del rin
     * @param diameter tamaño de diametro del rin
     * @param load carga del rin
     * @param material material del rin
     */
    public Rim(ComponentType type, int id, String name, String reference, Brand brand, Category category, String origin, String status, double width, double profile, double diameter, String load, String material) {
        super(type, id, name, reference, brand, category, origin, status);
        this.width = width;
        this.profile = profile;
        this.diameter = diameter;
        this.load = load;
        this.material = material;
    }

    /**
     * Convierte el objeto en una línea de registro.
     * @return Devuelve la línea para guardar en components.csv.
     */
    @Override
    public String toLine() {
        return FileUtil.join(getType().getFileValue(), getId(), getName(), getReference(), getBrand().getId(),
                getCategory().getId(), getOrigin(), getStatus(), width, profile, diameter, load, material);
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

    public String getLoad() {
        return load;
    }

    public void setLoad(String load) {
        this.load = load;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }
}
