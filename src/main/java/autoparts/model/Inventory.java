package autoparts.model;

import autoparts.util.FileUtil;

/**
 * Existencia de un componente en un proveedor: cuánto cuesta, cuántas unidades
 * hay, cuánto cuesta el envío y qué garantía ofrece. Cada fila de
 * {@code inventory.csv} es un objeto de esta clase.
 *
 * <p>Formato: {@code id|id_proveedor|id_componente|precio|stock|envío|garantía|condición|estado}.
 * Los valores monetarios son pesos colombianos enteros y la garantía está en meses.</p>
 *
 * @author Héctor Guevara
 */
public class Inventory {

    private static final int FIELD_COUNT = 9;

    private int id;
    private int supplierId;
    private int componentId;
    private int price;
    private int stock;
    private int shippingCost;
    private int warrantyMonths;
    private String condition;
    private Status status;

    /**
     * Crea una existencia de inventario.
     *
     * @param id             identificador de la fila
     * @param supplierId     identificador del proveedor (en {@code partners.csv})
     * @param componentId    identificador del componente (en {@code components.csv})
     * @param price          precio unitario en pesos
     * @param stock          unidades disponibles
     * @param shippingCost   costo del envío en pesos
     * @param warrantyMonths meses de garantía
     * @param condition      condición del componente: NUEVO, USADO o REACONDICIONADO
     * @param status         estado de la fila
     */
    public Inventory(int id, int supplierId, int componentId, int price, int stock,
            int shippingCost, int warrantyMonths, String condition, Status status) {
        this.id = id;
        this.supplierId = supplierId;
        this.componentId = componentId;
        this.price = price;
        this.stock = stock;
        this.shippingCost = shippingCost;
        this.warrantyMonths = warrantyMonths;
        this.condition = condition;
        this.status = status;
    }

    /**
     * Crea una existencia desde una línea de {@code inventory.csv}.
     *
     * @param line línea con los campos separados por "|"
     */
    public Inventory(String line) {
        String[] f = FileUtil.split(line);
        if (f.length != FIELD_COUNT) {
            throw new IllegalArgumentException("Una existencia de inventario tiene " + FIELD_COUNT + " campos.");
        }
        this.id = Integer.parseInt(f[0].trim());
        this.supplierId = Integer.parseInt(f[1].trim());
        this.componentId = Integer.parseInt(f[2].trim());
        this.price = Integer.parseInt(f[3].trim());
        this.stock = Integer.parseInt(f[4].trim());
        this.shippingCost = Integer.parseInt(f[5].trim());
        this.warrantyMonths = Integer.parseInt(f[6].trim());
        this.condition = f[7];
        this.status = Status.valueOf(f[8].trim());
    }

    /**
     * Devuelve la línea de esta existencia para {@code inventory.csv}.
     *
     * @return línea con los campos separados por "|"
     */
    public String toLine() {
        return FileUtil.join(id, supplierId, componentId, price, stock, shippingCost,
                warrantyMonths, condition, status);
    }

    /**
     * Indica si la fila está activa (no fue dada de baja).
     *
     * @return {@code true} si el estado es {@code ACTIVO}
     */
    public boolean isActive() {
        return status == Status.ACTIVO;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }
    public int getComponentId() { return componentId; }
    public void setComponentId(int componentId) { this.componentId = componentId; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public int getShippingCost() { return shippingCost; }
    public void setShippingCost(int shippingCost) { this.shippingCost = shippingCost; }
    public int getWarrantyMonths() { return warrantyMonths; }
    public void setWarrantyMonths(int warrantyMonths) { this.warrantyMonths = warrantyMonths; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}