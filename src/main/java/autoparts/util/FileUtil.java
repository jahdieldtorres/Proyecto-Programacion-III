
/**
 *
 * @author alvaro
 */

package autoparts.util;

import autopartes.exception.PersistenceException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/** Lee y escribe archivos planos: un registro por línea, campos separados por "|", UTF-8. */
public final class FileUtil {

    /** Separador de campos. */
    public static final String SEPARATOR = "|";

    private FileUtil() {
    }

    /** Devuelve las líneas de datos (sin vacías ni con #). Si el archivo no existe, lista vacía.
     * @param path      Dirección del archivo en el que persisten las líneas de datos
     * @return          Lista parametrizada en String con líneas de registros
     */
    public static List<String> read(String path) {
        List<String> lines = new ArrayList<>();
        Path file = Paths.get(path);
        if (!Files.exists(file)) {
            return lines;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (!line.isBlank() && !line.startsWith("#")) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("No se pudo leer el archivo: " + path, e);
        }
        return lines;
    }

    /** Igual que {@link #read}, pero cada línea ya separada en campos.
     * @param path      Dirección del archivo en el que persisten las líneas de datos
     * @return          Lista parametrizada en String con líneas de registros
     */
    public static List<String[]> readFields(String path) {
        List<String[]> records = new ArrayList<>();
        for (String line : read(path)) {
            records.add(split(line));
        }
        return records;
    }

    /** Reemplaza todo el contenido del archivo (se usa para actualizar y eliminar).
     * @param path      Dirección del archivo en el que persisten las líneas de datos
     * @param lines     Línea de registro
     */
    public static void rewrite(String path, List<String> lines) {
        write(path, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /** Agrega un registro al final del archivo (lo crea si no existe).
     * @param path      Dirección del archivo en el que persisten las líneas de datos
     * @param line      Línea de registro
     */
    public static void append(String path, String line) {
        write(path, List.of(line), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    /** Siguiente ID libre (máximo + 1). idColumn: 0 en archivos simples, 1 si el primer campo es el tipo.
     * @param path      Dirección del archivo en el que persisten las líneas de datos
     * @param idColumn  Índice de columna
     * @return          Índice de la siguiente columna libre
     */
    public static int nextId(String path, int idColumn) {
        int max = 0;
        for (String[] fields : readFields(path)) {
            try {
                max = Math.max(max, Integer.parseInt(fields[idColumn].trim()));
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                throw new PersistenceException("ID inválido en " + path + ": " + String.join(SEPARATOR, fields), e);
            }
        }
        return max + 1;
    }

    /** Une campos en una línea (para toText()). Null se guarda vacío; "|" o saltos de línea no se permiten.
     * @param fields        Campos del registro
     * @return              Línea de registro
     */
    public static String join(Object... fields) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            String value = fields[i] == null ? "" : String.valueOf(fields[i]);
            if (value.contains(SEPARATOR) || value.contains("\n") || value.contains("\r")) {
                throw new IllegalArgumentException("El valor no puede contener '|' ni saltos de línea: " + value);
            }
            line.append(i > 0 ? SEPARATOR : "").append(value);
        }
        return line.toString();
    }

    /** Separa una línea en campos, conservando los vacíos del final.
     * @param line      Línea de registro
     * @return          Campos del registro
     */
    public static String[] split(String line) {
        return line.split("\\|", -1);
    }

    private static void write(String path, List<String> lines, StandardOpenOption... options) {
        try {
            Path file = Paths.get(path).toAbsolutePath();
            Files.createDirectories(file.getParent());
            Files.write(file, lines, StandardCharsets.UTF_8, options);
        } catch (IOException e) {
            throw new PersistenceException("No se pudo escribir el archivo: " + path, e);
        }
    }
}