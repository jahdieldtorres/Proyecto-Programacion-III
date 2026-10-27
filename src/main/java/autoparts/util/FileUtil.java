package autopartes.util;

import autopartes.exception.PersistenceException;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilidades para leer y escribir los archivos planos del proyecto.
 *
 * <p>Formato: una línea por registro, campos separados por {@code |},
 * codificación UTF-8 y sin encabezado. Los saltos de línea que se escriben
 * son siempre {@code \n}, sin importar el sistema operativo, para que Git no
 * muestre diferencias falsas entre integrantes.</p>
 *
 * <p>Solo los DAO de {@code dao.archivo} deben usar esta clase. Todos los
 * errores de entrada/salida se envuelven en {@link PersistenceException}.</p>
 */
public final class FileUtil {

    /** Separador de campos dentro de una línea. */
    public static final String SEPARATOR = "|";

    private static final String SEPARATOR_REGEX = "\\|";
    private static final String COMMENT_PREFIX = "#";
    private static final char BOM = '\uFEFF';

    /** Clase de utilidades: no se instancia. */
    private FileUtil() {
    }

    /**
     * Lee las líneas de datos de un archivo.
     *
     * <p>Se ignoran las líneas vacías y las que empiezan por {@code #}.
     * Si el archivo no existe, devuelve una lista vacía (así un módulo
     * nuevo funciona antes de que exista su archivo).</p>
     *
     * @param path ruta del archivo (ver {@link FlatFiles})
     * @return lista modificable con una cadena por registro
     * @throws PersistenceException si el archivo no se puede leer
     */
    public static List<String> read(String path) {
        Path file = Paths.get(path);
        List<String> lines = new ArrayList<>();
        if (!Files.exists(file)) {
            return lines;
        }
        try {
            List<String> raw = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (int i = 0; i < raw.size(); i++) {
                String line = raw.get(i);
                // Algunos editores de Windows agregan una marca BOM al inicio.
                if (i == 0 && !line.isEmpty() && line.charAt(0) == BOM) {
                    line = line.substring(1);
                }
                if (line.isBlank() || line.startsWith(COMMENT_PREFIX)) {
                    continue;
                }
                lines.add(line);
            }
        } catch (IOException e) {
            throw new PersistenceException("No se pudo leer el archivo: " + path, e);
        }
        return lines;
    }

    /**
     * Lee un archivo y separa cada línea en sus campos.
     *
     * <p>Los campos finales vacíos se conservan (por ejemplo,
     * {@code "a|b|"} produce 3 campos).</p>
     *
     * @param path ruta del archivo
     * @return lista con un arreglo de campos por registro
     * @throws PersistenceException si el archivo no se puede leer
     */
    public static List<String[]> readFields(String path) {
        List<String[]> records = new ArrayList<>();
        for (String line : read(path)) {
            records.add(split(line));
        }
        return records;
    }

    /**
     * Reemplaza todo el contenido del archivo por las líneas indicadas.
     *
     * <p>Se usa para actualizar y eliminar: se lee todo, se cambia la lista
     * y se reescribe. Primero escribe en un archivo temporal y luego lo
     * mueve, así un fallo a mitad de la escritura no deja el archivo
     * original dañado. Si el archivo o su carpeta no existen, se crean.</p>
     *
     * @param path  ruta del archivo
     * @param lines líneas a guardar (puede estar vacía)
     * @throws PersistenceException si no se puede escribir
     */
    public static void rewrite(String path, List<String> lines) {
        Path file = Paths.get(path);
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            createParentDirectories(file);
            StringBuilder content = new StringBuilder();
            for (String line : lines) {
                checkSingleLine(line);
                content.append(line).append('\n');
            }
            Files.write(temp, content.toString().getBytes(StandardCharsets.UTF_8));
            try {
                Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            try {
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {
                // Si no se puede borrar el temporal, se informa el error principal.
            }
            throw new PersistenceException("No se pudo escribir el archivo: " + path, e);
        }
    }

    /**
     * Agrega una línea al final del archivo. Si el archivo no existe, lo crea.
     *
     * <p>Si el último registro del archivo no termina en salto de línea
     * (por ejemplo, por una edición manual), lo agrega antes de escribir
     * para no pegar dos registros en la misma línea.</p>
     *
     * @param path ruta del archivo
     * @param line registro nuevo, sin salto de línea
     * @throws PersistenceException si no se puede escribir
     */
    public static void append(String path, String line) {
        Path file = Paths.get(path);
        checkSingleLine(line);
        try {
            createParentDirectories(file);
            boolean needsBreak = Files.exists(file)
                    && Files.size(file) > 0
                    && !endsWithNewline(file);
            String text = (needsBreak ? "\n" : "") + line + "\n";
            Files.write(file, text.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new PersistenceException("No se pudo agregar al archivo: " + path, e);
        }
    }

    /**
     * Calcula el siguiente identificador numérico de un archivo: el mayor
     * existente más uno, o 1 si el archivo está vacío.
     *
     * @param path     ruta del archivo
     * @param idColumn posición (desde 0) de la columna del identificador.
     *                 Es 0 en archivos simples y 1 en los que tienen el tipo
     *                 como primer campo (socios, componentes, vehículos).
     * @return el siguiente identificador libre
     * @throws PersistenceException si alguna línea no tiene un identificador válido
     */
    public static int nextId(String path, int idColumn) {
        int max = 0;
        for (String[] fields : readFields(path)) {
            try {
                max = Math.max(max, Integer.parseInt(fields[idColumn].trim()));
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                throw new PersistenceException(
                        "Línea con identificador inválido en " + path + ": "
                                + String.join(SEPARATOR, fields), e);
            }
        }
        return max + 1;
    }

    /**
     * Une campos en una línea lista para guardar. Es la herramienta para
     * implementar {@code toTexto()} en las entidades del modelo.
     *
     * <p>Los valores {@code null} se guardan como cadena vacía.</p>
     *
     * @param fields valores en el orden definido en {@code docs/formatos-archivos.md}
     * @return la línea con los campos separados por {@code |}
     * @throws IllegalArgumentException si un valor contiene {@code |} o un
     *                                  salto de línea, porque dañaría el archivo
     *                                  (el servicio debe validarlo antes)
     */
    public static String join(Object... fields) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            String value = fields[i] == null ? "" : String.valueOf(fields[i]);
            if (value.contains(SEPARATOR) || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
                throw new IllegalArgumentException(
                        "El valor no puede contener '|' ni saltos de línea: " + value);
            }
            if (i > 0) {
                line.append(SEPARATOR);
            }
            line.append(value);
        }
        return line.toString();
    }

    /**
     * Separa una línea en sus campos, conservando los campos finales vacíos.
     *
     * @param line registro del archivo
     * @return arreglo con los campos
     */
    public static String[] split(String line) {
        return line.split(SEPARATOR_REGEX, -1);
    }

    // ---------------------------------------------------------------- privados

    private static void createParentDirectories(Path file) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    private static void checkSingleLine(String line) {
        if (line == null || line.indexOf('\n') >= 0 || line.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("Cada registro debe ser una sola línea no nula.");
        }
    }

    private static boolean endsWithNewline(Path file) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r")) {
            raf.seek(raf.length() - 1);
            return raf.read() == '\n';
        }
    }
}
