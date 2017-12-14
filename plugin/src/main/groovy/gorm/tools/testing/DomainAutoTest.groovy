package gorm.tools.testing

import org.springframework.core.GenericTypeResolver

trait DomainAutoTest<D> implements DaoDataTest{

    D domainInstance
    Class<D> domainClass // the domain class this is for

    Class<D> getDomainClass() {
        if (!domainClass) this.domainClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), DomainAutoTest.class)
        return domainClass
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
}
