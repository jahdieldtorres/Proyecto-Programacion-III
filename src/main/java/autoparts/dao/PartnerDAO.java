package autoparts.dao;

import autoparts.model.Partner;

import java.util.List;
import java.util.Optional;

/**
 * Operaciones para guardar y consultar socios (proveedores y talleres).
 *
 * Los servicios dependen de esta interfaz y no de cómo se guardan los datos:
 * hoy la implementa PartnerFileDAO (archivos planos) y después podrá
 * implementarla una clase para base de datos sin cambiar el servicio.
 *
 * @author Héctor Guevara
 */
public interface PartnerDAO {

    /**
     * Calcula el siguiente identificador libre.
     *
     * @return identificador para un socio nuevo
     */
    int nextId();

    /**
     * Guarda un socio nuevo.
     *
     * @param partner socio a guardar
     */
    void save(Partner partner);

    /**
     * Reemplaza los datos de un socio existente.
     *
     * @param partner socio con los datos nuevos
     * @return true si el socio existía y se actualizó
     */
    boolean update(Partner partner);

    /**
     * Busca un socio por su identificador.
     *
     * @param id identificador del socio
     * @return el socio, o vacío si no existe
     */
    Optional<Partner> findById(int id);

    /**
     * Busca un socio por su NIT, sin importar mayúsculas o minúsculas.
     *
     * @param taxId NIT del socio
     * @return el socio, o vacío si no existe
     */
    Optional<Partner> findByTaxId(String taxId);

    /**
     * Devuelve todos los socios, activos e inactivos.
     *
     * @return lista de socios
     */
    List<Partner> findAll();
}
