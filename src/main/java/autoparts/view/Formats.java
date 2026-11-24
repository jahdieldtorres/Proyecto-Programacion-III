package autoparts.view;

import autoparts.model.Condition;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Formatos de texto que comparten varias pantallas, para escribirlos una
 * sola vez.
 *
 * @author Jahdiel Torres
 */
public final class Formats {

    private static final NumberFormat MONEY = NumberFormat.getIntegerInstance(Locale.of("es", "CO"));

    private Formats() {
    }

    /**
     * Escribe un valor en pesos colombianos, por ejemplo 172000 → "$172.000".
     *
     * @param value valor en pesos
     * @return el valor con signo y separador de miles
     */
    public static String money(int value) {
        return "$" + MONEY.format(value);
    }

    /**
     * Escribe la condición con la primera letra en mayúscula, por ejemplo NUEVO → "Nuevo".
     *
     * @param condition condición del repuesto
     * @return el texto para mostrar
     */
    public static String condition(Condition condition) {
        String name = condition.name().toLowerCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * Escribe "1 almacén" o "N almacenes" según la cantidad.
     *
     * @param count número de almacenes
     * @return el texto en singular o plural
     */
    public static String stores(int count) {
        return count == 1 ? "1 almacén" : count + " almacenes";
    }
}
