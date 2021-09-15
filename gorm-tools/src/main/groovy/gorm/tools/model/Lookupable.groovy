package gorm.tools.model

/**
 * for GormEntity this marks it for a lookup static method, like a `get`,
 * that can get an eintity by other key fields other than just id
 */
trait Lookupable<D> {

    /**
     * lookup and get an entity based on keys in the data map
     * @param data the map with te other key data
     * @return the found entity or null if nothing was found
     */
    static D lookup(Map data){
        throw new UnsupportedOperationException("This method must be implemented in the class that has this trait")
    }
}
