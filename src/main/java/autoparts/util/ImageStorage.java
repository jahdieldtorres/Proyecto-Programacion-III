package autoparts.util;

import autoparts.exception.PersistenceException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

/**
 * Guarda las fotos en carpetas de la raíz del proyecto, igual que {@code data/}.
 * Las carpetas se crean solas si no existen:
 * <ul>
 *   <li>{@code images/components/}: fotos generales de los componentes
 *       (tabla COMPONENT_IMAGES).</li>
 *   <li>{@code images/inventory/}: fotos de cada oferta de una tienda
 *       (tabla INVENTORY_IMAGES).</li>
 * </ul>
 * En la base de datos solo se guarda el nombre del archivo; con ese nombre,
 * {@link #resolve(String)} devuelve la ruta de la foto para mostrarla.
 *
 * Los métodos sin carpeta trabajan con {@link #IMAGES_DIR} (componentes). Los
 * que reciben la carpeta sirven para las fotos de inventario: se les pasa
 * {@link #INVENTORY_DIR}.
 *
 * Para que dos fotos nunca se pisen, al copiar se le agrega al nombre original
 * un código corto al azar.
 *
 * @author Álvaro Aramendiz
 */
public final class ImageStorage {

    /** Carpeta de las fotos, relativa a la raíz del proyecto. */
    public static final String IMAGES_DIR = "images/components";

    /** Carpeta de las fotos de las ofertas de inventario, relativa a la raíz del proyecto. */
    public static final String INVENTORY_DIR = "images/inventory";

    private static final String[] EXTENSIONS = {"jpg", "jpeg", "png"};

    private ImageStorage() {
    }

    /**
     * Indica si el archivo es una foto que se puede guardar (.jpg, .jpeg o .png).
     * Sirve para validar lo que elige la persona antes de llamar a {@link #store(File)}.
     *
     * @param file archivo elegido; puede ser null
     * @return true si existe y tiene una extensión permitida
     */
    public static boolean isSupported(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }
        String extension = extensionOf(file.getName());
        for (String allowed : EXTENSIONS) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Copia la foto a {@code images/components/} y devuelve el nombre con el
     * que quedó. Ese nombre es el que se guarda en la base.
     *
     * @param source archivo de la foto elegida (.jpg, .jpeg o .png)
     * @return nombre del archivo copiado, por ejemplo {@code pastillas-freno-3f9a1c2b.jpg}
     * @throws PersistenceException si el archivo no es válido o no se pudo copiar
     */
    public static String store(File source) {
        return store(source, IMAGES_DIR);
    }

    /**
     * Copia la foto a la carpeta indicada y devuelve el nombre con el que
     * quedó. Ese nombre es el que se guarda en la base.
     *
     * @param source archivo de la foto elegida (.jpg, .jpeg o .png)
     * @param directory carpeta destino, por ejemplo {@link #INVENTORY_DIR}
     * @return nombre del archivo copiado
     * @throws PersistenceException si el archivo no es válido o no se pudo copiar
     */
    public static String store(File source, String directory) {
        if (!isSupported(source)) {
            throw new PersistenceException("La foto debe ser un archivo .jpg o .png que exista.");
        }
        String fileName = uniqueName(source.getName());
        Path target = Path.of(directory).resolve(fileName);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(source.toPath(), target);
        } catch (IOException e) {
            throw new PersistenceException("No se pudo guardar la foto en " + directory + ".", e);
        }
        return fileName;
    }

    /**
     * Devuelve la ruta de una foto guardada. Solo usa el nombre del archivo:
     * si llega con carpetas (por ejemplo {@code ../data/users.csv}) se ignoran,
     * así nunca se sale de la carpeta de fotos.
     *
     * @param fileName nombre guardado en la base de datos
     * @return ruta de la foto, o null si el nombre está vacío
     */
    public static Path resolve(String fileName) {
        return resolve(IMAGES_DIR, fileName);
    }

    /**
     * Devuelve la ruta de una foto guardada en la carpeta indicada. Solo usa el
     * nombre del archivo: las carpetas que lleguen en él se ignoran.
     *
     * @param directory carpeta de la foto, por ejemplo {@link #INVENTORY_DIR}
     * @param fileName nombre guardado en la base de datos
     * @return ruta de la foto, o null si el nombre está vacío
     */
    public static Path resolve(String directory, String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }
        return Path.of(directory).resolve(Path.of(fileName).getFileName());
    }

    /**
     * Indica si la foto existe en el disco.
     *
     * @param fileName nombre guardado en la base de datos
     * @return true si el archivo existe
     */
    public static boolean exists(String fileName) {
        return exists(IMAGES_DIR, fileName);
    }

    /**
     * Indica si la foto existe en el disco, en la carpeta indicada.
     *
     * @param directory carpeta de la foto, por ejemplo {@link #INVENTORY_DIR}
     * @param fileName nombre guardado en la base de datos
     * @return true si el archivo existe
     */
    public static boolean exists(String directory, String fileName) {
        Path path = resolve(directory, fileName);
        return path != null && Files.isRegularFile(path);
    }

    /**
     * Borra la foto del disco. Si no existe no hace nada.
     *
     * @param fileName nombre guardado en la base de datos
     * @throws PersistenceException si el archivo existe pero no se pudo borrar
     */
    public static void delete(String fileName) {
        delete(IMAGES_DIR, fileName);
    }

    /**
     * Borra la foto de la carpeta indicada. Si no existe no hace nada.
     *
     * @param directory carpeta de la foto, por ejemplo {@link #INVENTORY_DIR}
     * @param fileName nombre guardado en la base de datos
     * @throws PersistenceException si el archivo existe pero no se pudo borrar
     */
    public static void delete(String directory, String fileName) {
        Path path = resolve(directory, fileName);
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new PersistenceException("No se pudo borrar la foto " + fileName + ".", e);
        }
    }

    /**
     * Arma un nombre seguro y único: el nombre original sin tildes, espacios
     * ni símbolos, más 8 caracteres al azar y la extensión en minúsculas.
     */
    private static String uniqueName(String originalName) {
        String extension = extensionOf(originalName);
        int dot = originalName.lastIndexOf('.');
        String base = TextUtil.normalize(dot > 0 ? originalName.substring(0, dot) : originalName)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        if (base.isEmpty()) {
            base = "foto";
        }
        if (base.length() > 60) {
            base = base.substring(0, 60);
        }
        String code = UUID.randomUUID().toString().substring(0, 8);
        return base + "-" + code + "." + extension;
    }

    private static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
