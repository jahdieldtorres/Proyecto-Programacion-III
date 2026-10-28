package autoparts.exception;

/**
 * Error de validación: el usuario ingresó datos incorrectos o incompletos.
 * Su mensaje se muestra tal cual en la pantalla.
 *
 * @author Jahdiel Torres
 */
public class InvalidDataException extends RuntimeException {

    public InvalidDataException(String message) {
        super(message);
    }
}
