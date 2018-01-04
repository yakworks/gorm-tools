package gorm.tools.testing

import gorm.tools.GormMetaUtils
import gorm.tools.beans.DateUtil
import gorm.tools.beans.IsoDateUtil
import grails.gorm.validation.DefaultConstrainedProperty
import grails.testing.gorm.DomainUnitTest
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.springframework.core.GenericTypeResolver
import spock.lang.Shared

@SuppressWarnings(['JUnitPublicNonTestMethod', 'JUnitLostTest', 'JUnitTestMethodWithoutAssert', 'AbstractClassWithoutAbstractMethod'])
abstract class DomainAutoTest<D> extends GormToolsHibernateSpec implements DomainUnitTest<D> {

    D domainInstance
    Class<D> domainClass // the domain class this is for

    Class<D> getDomainClass() {
        if (!domainClass) this.domainClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), DomainAutoTest.class)
        return domainClass
    }

    @Shared
    Map values = [:]
    // map of values for domain, build based on `example`, includes values for required nested associations

    /**
     *
     * @return persistent entity
     */
    PersistentEntity getPersistentEntity(String domainClassName = getDomainClass().name) {
        GormMetaUtils.getPersistentEntity(domainClassName)
    }

    /**
     *
     * @return list of persistent properties
     */
    List<PersistentProperty> getPersistentProperties(PersistentEntity persistentEntity) {
        persistentEntity.persistentProperties
    }

    //Map of constrains properties for class
    Map getConstrainedProperties(PersistentEntity persistentEntity) {
        GormMetaUtils.findConstrainedProperties(persistentEntity)
    }

    /**
     *
     * setup values from `example` of constraints
     *
     * @return map with values from constraint example
     */
    Map fillValues(PersistentEntity persistentEntity) {
        Map result = [:]
        getPersistentProperties(persistentEntity).each { PersistentProperty property ->
            DefaultConstrainedProperty constrain = getConstrainedProperties(persistentEntity)[property.getName()]
            if (property instanceof Association) {
                if (!constrain.getAppliedConstraint("nullable").nullable) {
                    Class<PersistentEntity> pe = getPersistentEntity(property.associatedEntity.javaClass.name)
                    result[property.getName()] = pe.newInstance(fillValues(getPersistentEntity(property.associatedEntity.javaClass.name)))
                }
            } else {
                result[property.getName()] = property.type == Date ? IsoDateUtil.parse(constrain?.metaConstraints?.example?:"") : constrain?.metaConstraints?.example
            }
        }
        result
    }

    /** this is called by the {@link org.grails.testing.gorm.spock.DataTestSetupSpecInterceptor} */
    Class<?>[] getDomainClassesToMock() {
        [getDomainClass()].toArray(Class)
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

    @Override
    void setupSpec() {
        values = fillValues(getPersistentEntity())
    }

    void test_create() {
        when:
        D entity = getDomainClass().create(values)
        then:
        entity.id != null
    }

    void test_update() {
        setup:
        D entity = getDomainClass().create(values)
        Map values = values
        values.id = entity.id
        when:
        getDomainClass().update(values)
        then:
        getDomainClass().get(values.id) != null
    }

    void test_persist() {
        when:
        D entity = getDomainClass().newInstance(values)
        entity.persist()
        then:
        entity.id != null
    }

    void test_delete() {
        setup:
        D entity = getDomainClass().create(values)
        assert getDomainClass().get(entity.id) != null
        when:
        entity.delete()
        then:
        getDomainClass().get(entity.id) == null
    }

}
