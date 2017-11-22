package gorm.tools.dao
/**
* API for instance methods defined by a GORM entity
*
* @author Joshua Burnett
* @since 6.1
*
* @param <D> The domain entity class
*/
interface DaoEntityApi<D> {

    D persist(Map args)

    D persist()

    void remove()

    /**
     * Create and save with the parameters
     *
     * @return Returns the instance
     */
    D create(Map params)

    /**
     * Update using bindUpdate with params and persist.
     * Throws ValidationError or one of the Spring DataAccessExceptions if not successful
     *
     * @return Returns the instance
     */
    D update(Map params)

    /**
     * generates and returns the id for this.
     * Requires id generators to be setup
     */
    Serializable generateId()

    D bind(Map props)

    D bindCreate(Map props)

    D bindUpdate(Map props)

}
