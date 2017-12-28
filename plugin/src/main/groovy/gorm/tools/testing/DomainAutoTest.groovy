package gorm.tools.testing

import gorm.tools.GormMetaUtils
import grails.gorm.validation.DefaultConstrainedProperty
import grails.testing.gorm.DomainUnitTest
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.springframework.core.GenericTypeResolver

@SuppressWarnings(['JUnitPublicNonTestMethod', 'JUnitLostTest', 'JUnitTestMethodWithoutAssert', 'AbstractClassWithoutAbstractMethod'])
abstract class DomainAutoTest<D> extends GormToolsHibernateSpec implements DomainUnitTest<D> {

    D domainInstance
    Class<D> domainClass // the domain class this is for

    Class<D> getDomainClass() {
        if (!domainClass) this.domainClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), DomainAutoTest.class)
        return domainClass
    }

    /**
     *
     * @return persistent entity
     */
    PersistentEntity getPersistentEntity() {
        GormMetaUtils.getPersistentEntity(getDomainClass().name)
    }

    /**
     *
     * @return list of persistent properties
     */
    List<PersistentProperty> getPersistentProperties() {
        getPersistentEntity().persistentProperties
    }

    //Map of constrains properties for class
    Map getConstrainedProperties() {
        getDomainClass().constrainedProperties
    }

    /**
     *
     * setup values from `example` of constraints
     *
     * @return map with values from constraint example
     */
    Map values() {
        Map result = [:]
        getPersistentProperties().each {
            DefaultConstrainedProperty property = getConstrainedProperties()[it.getName()]?.property
            result[it.getName()] = property?.metaConstraints?.example
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

    void test_create() {
        when:
        D entity = getDomainClass().create(values())
        then:
        entity.id != null
    }

    void test_update() {
        setup:
        D entity = getDomainClass().create(values())
        Map values = values()
        values.id = entity.id
        when:
        getDomainClass().update(values)
        then:
        getDomainClass().get(values.id) != null
    }

    void test_persist() {
        when:
        D entity = getDomainClass().newInstance(values())
        entity.persist()
        then:
        entity.id != null
    }

    void test_delete() {
        setup:
        D entity = getDomainClass().create(values())
        assert getDomainClass().get(entity.id) != null
        when:
        entity.delete()
        then:
        getDomainClass().get(entity.id) == null
    }

}
