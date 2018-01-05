package gorm.tools.testing

import grails.testing.gorm.DomainUnitTest
import org.springframework.core.GenericTypeResolver
import spock.lang.Shared

@SuppressWarnings(['JUnitPublicNonTestMethod', 'JUnitLostTest', 'JUnitTestMethodWithoutAssert', 'AbstractClassWithoutAbstractMethod'])
abstract class DomainAutoTest<D> extends GormToolsHibernateSpec implements DomainUnitTest<D> {

    /** this is called by the {@link org.grails.testing.gorm.spock.DataTestSetupSpecInterceptor} */
    Class<?>[] getDomainClassesToMock() {
        [getDomainClass()].toArray(Class)
    }

    Class<D> domainClass // the domain class this is for

    Class<D> getDomainClass() {
        if (!domainClass) this.domainClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), DomainAutoTest.class)
        return domainClass
    }

    @Shared
    BuildExampleData<D> buildExampleData = BuildExampleHolder.get(getDomainClass())

    @Shared
    Map values = [:]

    @Override
    void setupSpec() {
        values = buildExampleData.buildValues()
    }

    //TODO: think about more intelligent way to override default test methods

    void test_create() {
        when:
        D entity = getDomainClass().create(values)
        then:
        entity.id != null
    }

    void test_update() {
        setup:
        D entity = getDomainClass().create(values)
        //Map values = values
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

    void cleanupSpec(){
        BuildExampleHolder.clear()
    }

}
