package autoparts.model;

import autoparts.util.FileUtil;

/**
 * Representa el componente de llanta de un vehículo, detallando sus medidas e índices.
 * 
 * @author ALVARO
 */
public class Tire extends Component {

    private int widthMm;
    private int profile;
    private int diameterInches;
    private int loadIndex;
    private String speedIndex;

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
     * @param widthMm ancho de la llanta en milímetros
     * @param profile perfil de la llanta
     * @param diameterInches diámetro de la llanta en pulgadas
     * @param loadIndex índice de carga de la llanta
     * @param speedIndex índice de velocidad de la llanta
     */
    public Tire(ComponentType type, int id, String name, String reference, Brand brand, Category category, String origin, String status, int widthMm, int profile, int diameterInches, int loadIndex, String speedIndex) {
        super(type, id, name, reference, brand, category, origin, status);
        this.widthMm = widthMm;
        this.profile = profile;
        this.diameterInches = diameterInches;
        this.loadIndex = loadIndex;
        this.speedIndex = speedIndex;
    }

    /**
     * Convierte el objeto en una línea de registro.
     * @return Devuelve la línea para guardar en components.csv.
     */
    @Override
    public String toLine() {
        return FileUtil.join(getType().getFileValue(), getId(), getName(), getReference(), getBrand().getId(),
                getCategory().getId(), getOrigin(), getStatus(), widthMm, profile, diameterInches, loadIndex, speedIndex);
    }

    public int getWidthMm() {
        return widthMm;
    }

    public void setWidthMm(int widthMm) {
        this.widthMm = widthMm;
    }

    public int getProfile() {
        return profile;
    }

    public void setProfile(int profile) {
        this.profile = profile;
    }

    public int getDiameterInches() {
        return diameterInches;
    }

    public void setDiameterInches(int diameterInches) {
        this.diameterInches = diameterInches;
    }

    public int getLoadIndex() {
        return loadIndex;
    }

    public void setLoadIndex(int loadIndex) {
        this.loadIndex = loadIndex;
    }

    public String getSpeedIndex() {
        return speedIndex;
    }

    public void setSpeedIndex(String speedIndex) {
        this.speedIndex = speedIndex;
    }
}