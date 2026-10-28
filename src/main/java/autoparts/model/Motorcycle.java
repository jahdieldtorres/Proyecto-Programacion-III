package autoparts.model;

import autoparts.util.FileUtil;

/**
 * Representa una motocicleta en el sistema.
 * 
 * @author ALVARO
 */
public class Motorcycle extends Vehicle {

    private int cylinders;
    private MotorcycleType type;

    /**
     * Crea una nueva instancia de Motorcycle con los valores especificados.
     * 
     * @param vehicleType el tipo general del vehículo
     * @param id el identificador del vehículo
     * @param brand la marca del vehículo
     * @param modelName el nombre del modelo del vehículo
     * @param yearRelease el año de lanzamiento del vehículo
     * @param status el estado cualitativo del vehículo
     * @param cylinders el número de cilindros de la motocicleta
     * @param type el tipo específico de la motocicleta
     */
    public Motorcycle(VehicleType vehicleType, String id, Brand brand, String modelName, int yearRelease, String status, int cylinders, MotorcycleType type) {
        super(vehicleType.MOTOCICLETA, id, brand, modelName, yearRelease, status);
        this.cylinders = cylinders;
        this.type = type;
    }

    /**
     * Convierte el objeto en una línea de registro.
     * @return Devuelve la línea para guardar en el archivo.
     */
    @Override
    public String toLine() {
        return FileUtil.join(getType().getFileValue(), getId(), getBrand().getName(), getModelName(), getYearRelease(), getStatus(), cylinders, type.getFileValue());
    }

    public int getCylinders() {
        return cylinders;
    }

    public void setCylinders(int cylinders) {
        this.cylinders = cylinders;
    }

    public MotorcycleType getMotorcycleType() {
        return type;
    }

    public void setMotorcycleType(MotorcycleType type) {
        this.type = type;
    }
}