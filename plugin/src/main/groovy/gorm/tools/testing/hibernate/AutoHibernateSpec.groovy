package gorm.tools.testing.hibernate

import gorm.tools.testing.TestDataJson
import grails.buildtestdata.TestData
import groovy.transform.CompileDynamic
import org.springframework.core.GenericTypeResolver

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

    Map buildCreateMap(Map args = [:]) {
        buildMap(args)
    }

    Map buildUpdateMap(Map args = [:]) {
        buildMap(args)
    }

    D buildCreate(Map args = [:]) {
        buildMap(args)
    }

    void testCreate() {
        when:
        D entity = entityClass.create(buildCreateMap())
        Long id = entity.id
        flushAndClear()

        then:
        entityClass.get(id).id

    }

    void testUpdate() {
        given:
        D entity = entityClass.create(buildCreateMap())
        assert entity.version == 0
        Long id = entity.id
        flushAndClear()

        when:
        Map updateMap = buildUpdateMap()
        updateMap.id = id
        entityClass.update(updateMap)
        flushAndClear()
        D upInstance = entityClass.get(id)

        then:
        upInstance.id
        upInstance.version == 1
    }

    void testPersist() {
        when:
        D entity = TestData.build(entityClass, save:false)
        entity.persist()
        Long id = entity.id
        flushAndClear()

        then:
        entityClass.get(id)
    }

    void testRemove() {
        setup:
        D entity = TestData.build(entityClass)
        Long id = entity.id
        flushAndClear()

        when:
        entityClass.get(id).remove()

        then:
        entityClass.get(id) == null
    }

}
