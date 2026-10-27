package autopartes.exception;

/**
 * Excepción que envuelve los errores de lectura o escritura de los archivos
 * planos (por ejemplo, un {@link java.io.IOException}) para que las capas
 * superiores no dependan de detalles de entrada/salida.
 *
 * <p>Es una excepción no verificada (hereda de {@link RuntimeException}) para
 * no obligar a declarar {@code throws} en cada método de las capas dao y
 * servicio. La capa de vista debe capturarla y mostrar un mensaje claro.</p>
 */
public class PersistenceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción con un mensaje descriptivo.
     *
     * @param message descripción del problema
     */
    public PersistenceException(String message) {
        super(message);
    }

    /**
     * Crea la excepción con un mensaje y la causa original.
     *
     * @param message descripción del problema
     * @param cause   excepción original (normalmente un IOException)
     */
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
