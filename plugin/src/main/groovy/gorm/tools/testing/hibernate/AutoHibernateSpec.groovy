package gorm.tools.testing.hibernate

import gorm.tools.testing.JsonifyUnitTest
import grails.buildtestdata.TestDataBuilder
import groovy.transform.CompileDynamic
import org.springframework.core.GenericTypeResolver

@SuppressWarnings(['JUnitPublicNonTestMethod', 'JUnitLostTest', 'JUnitTestMethodWithoutAssert', 'AbstractClassWithoutAbstractMethod'])
@CompileDynamic
abstract class AutoHibernateSpec<D> extends GormToolsHibernateSpec implements JsonifyUnitTest, TestDataBuilder {

    private Class<D> domainClass // the domain class this is for

    Class<D> getEntityClass() {
        if (!domainClass) this.domainClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), AutoHibernateSpec.class)
        return domainClass
    }

    @Override
    List<Class> getDomainClasses() { [getEntityClass()] }

    void testCreateRequired() {
        when:
        D entity = entityClass.create(buildJson().json)
        then:
        entity.id != null
    }

    void testUpdateRequired() {
        setup:
        D entity = entityClass.create(buildJson().json)
        Map values = buildJson().json
        values.id = entity.id
        when:
        entityClass.update(values)
        then:
        entityClass.get(values.id) != null
    }

    void testPersist() {
        when:
        D entity = entityClass.newInstance(buildJson().json)
        entity.persist()
        then:
        entity.id != null
    }

    void testDelete() {
        setup:
        D entity = entityClass.create(buildJson().json)
        assert entityClass.get(entity.id) != null
        when:
        entity.delete()
        then:
        entityClass.get(entity.id) == null
    }

}
