package gorm.tools

import grails.util.GrailsNameUtils
import grails.util.Holders
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.orm.hibernate.cfg.Mapping

/**
 * A bunch of helper and lookup/finder statics for dealing with domain classes and PersistentEntity.
 * Useful methods to find the PersistentEntity and the mapping and meta fields.
 */
@CompileStatic
class GormMetaUtils {

    /**
     * Returns a persistent entity using the class.
     *
     * @param cls the domain class
     * @return The entity or null
     */
    static PersistentEntity getPersistentEntity(Class cls) {
        getMappingContext().getPersistentEntity(cls.name)
    }

    /**
     * Returns a persistent entity using a fully qualified name
     * with package such as "com.foo.Product".
     *
     * @param name an entity name which includes package
     * @return The entity or null
     */
    static PersistentEntity getPersistentEntity(String fullName) {
        getMappingContext().getPersistentEntity(fullName)
    }

    /**
     * Returns a persistent entity using a domain instance.
     *
     * Note: shortcut for getPersistentEntity(instance.class.name)
     *
     * @param instance the domain instance
     * @return The entity or null
     */
    static PersistentEntity getPersistentEntity(GormEntity instance) {
        getPersistentEntity(instance.class.name)
    }

    /**
     * finds domain using either a simple name like "Product" or fully qualified name "com.foo.Product"
     * name can start with either a lower or upper case
     *
     * @param name an entity name to search for
     * @return The entity or null
     */
    static PersistentEntity findPersistentEntity(String name) {
        if (name.indexOf('.') == -1) {
            String propertyName = GrailsNameUtils.getPropertyName(name)
            return getMappingContext().persistentEntities.find { PersistentEntity entity ->
                entity.decapitalizedName == propertyName
            }
        }
        return getPersistentEntity(name)
    }

    /**
     * the mapping grailsDomainClassMappingContext. This is the main holder for the persistentEntities
     * @return
     */
    static MappingContext getMappingContext() {
        Holders.applicationContext.getBean("grailsDomainClassMappingContext", MappingContext)
    }

    /**
     * Returns the mapping for the entity to DB.
     *
     * @param pe the PersistentEntity can found using the loolup statics above
     * @return the mapping for the entity to DB
     */
    @CompileDynamic
    Mapping getMapping(PersistentEntity pe) {
        return getMappingContext().mappingFactory?.entityToMapping?.get(pe)
    }

    /****** Older deprecated way with GrailsDomainClass *******/

//    static GrailsDomainClass getDomainClass(Class cls) {
//        return getDomainClass(cls.name)
//    }
//
//    static GrailsDomainClass getDomainClass(String fullName) {
//        return (GrailsDomainClass) Holders.grailsApplication.getArtefact(DomainClassArtefactHandler.TYPE, fullName)
//    }
//
//    static GrailsDomainClass getDomainClass(GormEntity instance) {
//        return getDomainClass(instance.getClass().getName())
//    }

    /**
     * finds domain using either a simple name like "Product" or fully qualified name "com.foo.Product"
     *
     * @param name a name of a domain class
     * @return The entity or null
     */
    //@CompileDynamic
//    static GrailsDomainClass findDomainClass(String name){
//        if(name.indexOf('.') == -1){
//            String propertyName = GrailsNameUtils.getPropertyName(name)
//            return Holders.grailsApplication.domainClasses.find { GrailsDomainClass dom ->
//                dom.propertyName == propertyName
//            }
//        }
//        return getDomainClass(name)
//    }

}
