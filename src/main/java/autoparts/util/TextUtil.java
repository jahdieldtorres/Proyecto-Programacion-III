package autoparts.util;

import java.text.Normalizer;

/**
 * Utilidades para trabajar con texto. Se usa en las búsquedas para que
 * "bateria", "Batería" y "BATERÍA" se consideren el mismo texto.
 *
 * @author Alvaro Aramendiz
 */
public final class TextUtil {

    private TextUtil() {
    }

    /**
     * Deja el texto listo para comparar: quita los espacios de los extremos,
     * las tildes y pasa todo a minúsculas.
     *
     * @param text texto original; puede ser null
     * @return texto sin tildes y en minúsculas; vacío si el original es null
     */
    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(text.trim(), Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}", "").toLowerCase();
    }
}
