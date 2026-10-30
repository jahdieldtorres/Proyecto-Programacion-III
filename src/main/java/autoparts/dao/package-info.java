/**
 * Capa de acceso a datos: interfaces DAO (UserDAO, ComponentDAO, OfferDAO...).
 *
 * Los servicios dependen solo de estas interfaces. Hoy se implementan con archivos planos
 * en autoparts.dao.file; cuando se conecte la base de datos se agregará
 * autoparts.dao.oracle sin cambiar los servicios ni las vistas.
 */
package autoparts.dao;
