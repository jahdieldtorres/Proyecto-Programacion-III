package autoparts.model;

import autoparts.util.FileUtil;

/**
 * Clase que describe el componente rin de vehículos
 * @author ALVARO
 */
public class Rim extends Component {
    private int diameterInches;
    private double widthInches;
    private String pcd;
    private RimMaterial material;

    /**
     * Crea la clase de componentes con un nombre descriptivo e identificador
     * único de postulamiento.
     *
     * @param id identificador de producto componente
     * @param name nombre descriptivo del componente
     * @param reference referencia de fabricante
     * @param brand marca del componente
     * @param category categoría del componente
     * @param origin origen del componente
     * @param status estado cualitativo del componente
     * @param diameterInches tamaño de diametro del rin en pulgadas
     * @param widthInches ancho del rin en pulgadas
     * @param pcd patrón de pernos del rin
     * @param material material del rin
     */
    public Rim(int id, String name, String reference, Brand brand, Category category, String origin, String status, int diameterInches, double widthInches, String pcd, RimMaterial material) {
        super(ComponentType.RIM, id, name, reference, brand, category, origin, status);
        this.diameterInches = diameterInches;
        this.widthInches = widthInches;
        this.pcd = pcd;
        this.material = material;
    }

    /**
     * Convierte el objeto en una línea de registro.
     * @return Devuelve la línea para guardar en components.csv.
     */
    @Override
    public String toLine() {
        return FileUtil.join(getType().getFileValue(), getId(), getName(), getReference(), getBrand().getId(),
                getCategory().getId(), getOrigin(), getStatus(), diameterInches, widthInches, pcd, material.getFileValue());
    }

    public int getDiameterInches() {
        return diameterInches;
    }

    public void setDiameterInches(int diameterInches) {
        this.diameterInches = diameterInches;
    }

    public double getWidthInches() {
        return widthInches;
    }

    public void setWidthInches(double widthInches) {
        this.widthInches = widthInches;
    }

    public String getPcd() {
        return pcd;
    }

    public void setPcd(String pcd) {
        this.pcd = pcd;
    }

    public RimMaterial getMaterial() {
        return material;
    }

    public void setMaterial(RimMaterial material) {
        this.material = material;
    }
}