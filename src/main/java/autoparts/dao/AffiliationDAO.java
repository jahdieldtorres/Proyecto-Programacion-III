package autoparts.dao;

import autoparts.model.Affiliation;

import java.util.List;
import java.util.Optional;

/**
 * Operaciones para guardar y consultar las afiliaciones entre proveedores y talleres.
 *
 * Los servicios dependen de esta interfaz y no de cómo se guardan los datos:
 * hoy la implementa AffiliationFileDAO (archivos planos) y después podrá
 * implementarla una clase para base de datos sin cambiar el servicio.
 *
 * @author Héctor Guevara
 */
public interface AffiliationDAO {

    /**
     * Calcula el siguiente identificador libre.
     *
     * @return identificador para una afiliación nueva
     */
    int nextId();

    /**
     * Guarda una afiliación nueva.
     *
     * @param affiliation afiliación a guardar
     */
    void save(Affiliation affiliation);

    /**
     * Reemplaza los datos de una afiliación existente.
     *
     * @param affiliation afiliación con los datos nuevos
     * @return true si la afiliación existía y se actualizó
     */
    boolean update(Affiliation affiliation);

    /**
     * Busca una afiliación por su identificador.
     *
     * @param id identificador de la afiliación
     * @return la afiliación, o vacío si no existe
     */
    Optional<Affiliation> findById(int id);

    /**
     * Devuelve las afiliaciones de un taller.
     *
     * @param workshopId identificador del taller
     * @return lista de afiliaciones del taller
     */
    List<Affiliation> findByWorkshop(int workshopId);

    /**
     * Devuelve las afiliaciones de un proveedor.
     *
     * @param supplierId identificador del proveedor
     * @return lista de afiliaciones del proveedor
     */
    List<Affiliation> findBySupplier(int supplierId);

    /**
     * Devuelve todas las afiliaciones.
     *
     * @return lista de afiliaciones
     */
    List<Affiliation> findAll();
}
