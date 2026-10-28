package autoparts.model;

import java.util.Comparator;

/**
 * Criterios con los que el comparador ordena las ofertas. En cada uno, la
 * mejor oferta queda primera. Si dos ofertas empatan, gana la de menor costo
 * total (o, en el costo total, la que entrega antes).
 *
 * @author Héctor Guevara
 */
public enum ComparisonCriterion {

    /** Menor precio del componente. */
    PRICE("Menor precio",
            Comparator.comparingInt(Offer::getPrice).thenComparingInt(Offer::getTotalCost)),

    /** Menor costo total (precio más envío). */
    TOTAL_COST("Menor costo total",
            Comparator.comparingInt(Offer::getTotalCost).thenComparingInt(Offer::getDeliveryDays)),

    /** Mayor garantía en meses. */
    WARRANTY("Mayor garantía",
            Comparator.comparingInt(Offer::getWarrantyMonths).reversed()
                    .thenComparingInt(Offer::getTotalCost)),

    /** Menor tiempo de entrega en días. */
    DELIVERY("Entrega más rápida",
            Comparator.comparingInt(Offer::getDeliveryDays).thenComparingInt(Offer::getTotalCost));

    private final String label;
    private final Comparator<Offer> comparator;

    ComparisonCriterion(String label, Comparator<Offer> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    /**
     * Devuelve el comparador que ordena las ofertas según este criterio.
     *
     * @return comparador de ofertas; la mejor queda primera
     */
    public Comparator<Offer> getComparator() {
        return comparator;
    }

    /**
     * Texto en español para mostrar el criterio en pantalla.
     *
     * @return nombre del criterio
     */
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}