package autoparts;

/**
 * Punto de entrada del ejecutable (ORIGEN.exe).
 *
 * Solo llama a {@link Main#main(String[])}. Hace falta porque cuando la clase
 * principal extiende Application y el programa arranca desde un .jar sin los
 * módulos de JavaFX, Java responde "faltan los componentes de JavaFX". Con una
 * clase intermedia que no extiende Application, JavaFX se carga desde el
 * classpath y la app abre bien.
 *
 * NetBeans sigue usando {@link Main} (ver pom.xml); esta clase es solo para
 * jpackage. No debe tener lógica propia.
 *
 * @author Héctor Guevara
 */
public class Launcher {

    private Launcher() {
    }

    /**
     * Arranca la aplicación igual que Main.
     *
     * @param args argumentos de la línea de comandos (no se usan)
     */
    public static void main(String[] args) {
        Main.main(args);
    }
}
