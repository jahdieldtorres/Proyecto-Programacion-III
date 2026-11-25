package autoparts.view;

import autoparts.model.Partner;
import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Abre WhatsApp con un mensaje ya escrito para un almacén o taller. La usan
 * las pantallas que muestran el botón "Escribir por WhatsApp".
 *
 * Usa la misma forma que MapController.openWhatsApp: el número de Colombia
 * (prefijo 57) y la dirección https://wa.me, que abre WhatsApp en el
 * navegador o en la app de escritorio.
 *
 * @author Héctor Guevara
 */
public final class WhatsApp {

    private WhatsApp() {
    }

    /**
     * Abre el chat con el socio y deja el mensaje escrito.
     *
     * @param partner almacén o taller al que se le escribe
     * @param text    mensaje que aparece listo para enviar
     * @return true si se pudo abrir; false si el computador no lo permite
     */
    public static boolean open(Partner partner, String text) {
        String phone = "57" + partner.getPhone().replaceAll("[^0-9]", "");
        String url = "https://wa.me/" + phone + "?text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
        try {
            Desktop.getDesktop().browse(new URI(url));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
