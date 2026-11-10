package autoparts.model;

import autoparts.util.FileUtil;

/**
 * Representa un vehículo de tipo automóvil en el sistema.
 * 
 * @author ALVARO
 */
public class Car extends Vehicle {

    private int numDoors;
    private CarType type;

    /**
     * Crea una nueva instancia de Car con los valores especificados.
     * 
     * @param id el identificador del vehículo
     * @param brand la marca del vehículo
     * @param modelName el nombre del modelo del vehículo
     * @param yearRelease el año de lanzamiento del vehículo
     * @param status el estado cualitativo del vehículo
     * @param numDoors el número de puertas del automóvil
     * @param type el tipo específico del automóvil
     */
    public Car(int id, Brand brand, String modelName, int yearRelease, String status, int numDoors, CarType type) {
        super(VehicleType.COCHE, id, brand, modelName, yearRelease, status);
        this.numDoors = numDoors;
        this.type = type;
    }

    /**
     * Convierte el objeto en una línea de registro.
     * @return Devuelve la línea para guardar en el archivo.
     */
    @Override
    public String toLine() {
        return FileUtil.join(getType().getFileValue(), getId(), getBrand().getName(), getModelName(), getYearRelease(), getStatus(), numDoors, type.getFileValue());
    }

    public int getNumDoors() {
        return numDoors;
    }

    public void setNumDoors(int numDoors) {
        this.numDoors = numDoors;
    }

    public CarType getCarType() {
        return type;
    }

    public void setCarType(CarType type) {
        this.type = type;
    }
    
    @Override
    public String getDetail() {
        return numDoors + " puertas - " + type;
    }
}