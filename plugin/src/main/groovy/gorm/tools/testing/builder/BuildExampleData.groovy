package gorm.tools.testing.builder

import gorm.tools.GormMetaUtils
import gorm.tools.beans.IsoDateUtil
import grails.gorm.validation.ConstrainedProperty
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association

@CompileStatic
class BuildExampleData<D> {

    D domainInstance
    Class<D> domainClass // the domain class this is for

    BuildExampleData(Class clazz){
        this.domainClass = clazz
    }

    /**
     *
     * @return persistent entity
     */
    PersistentEntity getPersistentEntity() {
        GormMetaUtils.getPersistentEntity(domainClass.name)
    }

    /**
     *
     * @return list of persistent properties
     */
    List<PersistentProperty> getPersistentProperties() {
        persistentEntity.persistentProperties
    }

    //Map of constrains properties for class
    Map getConstrainedProperties() {
        GormMetaUtils.findConstrainedProperties(persistentEntity)
    }

    /**
     *
     * setup values from `example` of constraints
     *
     * @return map with values from constraint example
     */
    @CompileDynamic
    Map buildValues() {
        Map result = [:]
        getPersistentProperties().each { PersistentProperty property ->
            ConstrainedProperty constrain = getConstrainedProperties()[property.getName()]
            if (property instanceof Association) {
                if (!constrain.isNullable()) {
                    result[property.getName()] = BuildExampleHolder.get(property.associatedEntity.javaClass).buildValues()
                }
            } else {
                result[property.getName()] = property.type == Date ? IsoDateUtil.parse(constrain?.metaConstraints?.example?:"") : constrain?.metaConstraints?.example
            }
        }
        result
    }

    /**
     * @return An instance of the domain class
     */
    D getDomain() {
        if (domainInstance == null) {
            domainInstance = getDomainClass().newInstance()
        }
        domainInstance
    }

}
