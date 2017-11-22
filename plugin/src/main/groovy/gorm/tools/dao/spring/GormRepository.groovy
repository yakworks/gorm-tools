package gorm.tools.dao.spring

import grails.plugin.dao.DomainException
import groovy.transform.CompileStatic
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository

@CompileStatic
interface GormRepository<T, ID> extends PagingAndSortingRepository<T, ID>, CrudRepository<T, ID> {

    //override this to set the domain this dao is for
    Class<T> getDomainClass()

    /**
     * saves a domain entity with the passed in args and rewraps ValidationException with DomainException on error
     *
     * @param entity the domain entity instance to call save on
     * @param args the arguments to pass to the Gorm Domain.save(args)
     * @return the saved entity
     * @throws grails.plugin.dao.DomainException if a validation or DataAccessException error happens
     */
    def <S extends T> S save(S entity, Map args)

    /**
     * Deletes an instance from the datastore
     */
    void delete(T entity, Map params)

    /**
     * creates entity using the data from params and calls save for a new domain entity
     *
     * @param params the parameter map
     * @return the created entity
     * @throws DomainException if a validation error happens
     */
    T create(Map params)

    /**
     * updates a new domain entity after binding data from params
     *
     * @param params the parameter map
     * @return the updated entity
     * @throws DomainException if a validation error happens or its not found with the params.id or the version is off and someone else edited it
     */
    T update(Map params)

    /**
     * deletes a new domain entity base on the id in the params
     *
     * @param params the parameter map that has the id for the domain entity to delete
     * @throws DomainException if its not found or if a DataIntegrityViolationException is thrown
     */
    Map remove(Map params)

    /**
     * Returns all instances of the type with the given IDs.
     *
     * @param ids
     * @return
     */
    @Override
    List<T> findAll(Iterable<ID> ids)

}
