package autoparts.dao;

import autoparts.model.Fee;

import java.util.List;
import java.util.Optional;

/**
 * Operaciones para guardar y consultar las tarifas de los servicios de los talleres.
 *
 * Los servicios dependen de esta interfaz y no de cómo se guardan los datos:
 * hoy la implementa FeeFileDAO (archivos planos) y después podrá implementarla
 * una clase para base de datos sin cambiar el servicio.
 *
 * @author Héctor Guevara
 */
public interface FeeDAO {

    /**
     * Calcula el siguiente identificador libre.
     *
     * @return identificador para una tarifa nueva
     */
    int nextId();

    /**
     * Guarda una tarifa nueva.
     *
     * @param fee tarifa a guardar
     */
    void save(Fee fee);

    /**
     * Reemplaza los datos de una tarifa existente.
     *
     * @param fee tarifa con los datos nuevos
     * @return true si la tarifa existía y se actualizó
     */
    boolean update(Fee fee);

    /**
     * Busca una tarifa por su identificador.
     *
     * @param id identificador de la tarifa
     * @return la tarifa, o vacío si no existe
     */
    Optional<Fee> findById(int id);

    /**
     * Devuelve las tarifas de un servicio, vigentes o no.
     *
     * @param serviceId identificador del servicio
     * @return lista de tarifas del servicio
     */
    List<Fee> findByService(int serviceId);

    /**
     * Devuelve todas las tarifas.
     *
     * @return lista de tarifas
     */
    List<Fee> findAll();
}
