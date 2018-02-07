package gorm.tools.testing.hibernate

import gorm.tools.testing.TestDataJson
import grails.buildtestdata.TestData
import groovy.transform.CompileDynamic
import org.springframework.core.GenericTypeResolver
//import static gorm.tools.testing.TestDataJson.buildCreate
//import static gorm.tools.testing.TestDataJson.buildMap

@SuppressWarnings(['JUnitPublicNonTestMethod', 'JUnitLostTest', 'JUnitTestMethodWithoutAssert', 'AbstractClassWithoutAbstractMethod'])
@CompileDynamic
abstract class AutoHibernateSpec<D> extends GormToolsHibernateSpec {

    private Class<D> domainClass // the domain class this is for

    Class<D> getEntityClass() {
        if (!domainClass) this.domainClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), AutoHibernateSpec.class)
        return domainClass
    }

    @Override
    List<Class> getDomainClasses() { [getEntityClass()] }

    Map buildMap(Map args = [:]) {
        TestDataJson.buildMap(args, getEntityClass())
    }

    D buildCreate(Map args = [:]) {
        TestDataJson.buildCreate(args, getEntityClass())
    }

    void testCreate() {
        when:
        D entity = entityClass.create(buildMap())
        then:
        entity.id != null
    }

    void testUpdate() {
        setup:
        D entity = entityClass.create(buildMap())
        Map values = buildMap()
        values.id = entity.id
        when:
        entityClass.update(values)
        then:
        entityClass.get(values.id) != null
    }

    void testPersist() {
        when:
        D entity = TestData.build(entityClass, save:false)
        entity.persist()
        then:
        entity.id != null
    }

    void testRemove() {
        setup:
        D entity = TestData.build(entityClass)
        assert entityClass.get(entity.id) != null
        when:
        entity.remove()
        then:
        entityClass.get(entity.id) == null
    }

}
