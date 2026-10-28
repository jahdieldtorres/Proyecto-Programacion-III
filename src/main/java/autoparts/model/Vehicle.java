package autoparts.model;

import autoparts.util.FileUtil;

/**
 * Clase abstracta que representa un vehículo en el sistema.
 * 
 * @author ALVARO
 */
public abstract class Vehicle {

    private VehicleType type;
    private int id;
    private Brand brand;
    private String modelName;
    private int yearRelease;
    private String status;

    /**
     * Crea una nueva instancia de Vehicle con los valores especificados.
     * 
     * @param type tipo del vehículo
     * @param id identificador del vehículo
     * @param brand marca del vehículo
     * @param modelName nombre del modelo del vehículo
     * @param yearRelease año de lanzamiento del vehículo
     * @param status estado cualitativo del vehículo
     */
    public Vehicle(VehicleType type, int id, Brand brand, String modelName, int yearRelease, String status) {
        this.type = type;
        this.id = id;
        this.brand = brand;
        this.modelName = modelName;
        this.yearRelease = yearRelease;
        this.status = status;
    }

    /**
     * Convierte el objeto en una línea de registro.
     * @return Devuelve la línea para guardar en el archivo.
     */
    public String toLine() {
        return FileUtil.join(type.getFileValue(), id, brand.getName(), modelName, yearRelease, status);
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public int getYearRelease() {
        return yearRelease;
    }

    public void setYearRelease(int yearRelease) {
        this.yearRelease = yearRelease;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
