package yakworks.security

import grails.artefact.DomainClass
import grails.web.databinding.WebDataBinding
import org.springframework.util.ReflectionUtils
import spock.lang.Specification
import yakworks.security.audit.StampedEntity
import yakworks.security.gorm.model.AppUser
import yakworks.testing.gorm.unit.SecurityTest
import yakworks.testing.gorm.unit.GormHibernateTest

/**
 * This will fail if the org.grails:grails-plugin-controllers is in compile dependency during compile of gorm domain
 * we dont want this and there is no way to to turn it off
 * we don't want it as it adds a map constructor that uses the slow Grails binder and ties domains to controllers
 * we want the default groovy map constructor or ability to use the Groovy @MapConstrutor AST transformation
 */
class NoControllersDomainBindingSpec extends Specification implements GormHibernateTest, SecurityTest {
    static List entityClasses = [AppUser]

    void "check for instanceControllersDomainBindingApi"() {
        expect:
        // if it has instanceControllersDomainBindingApi then it means it got compiled with controller
        ReflectionUtils.findField(AppUser, 'instanceControllersDomainBindingApi') == null
        ReflectionUtils.findField(AppUser, '$defaultDatabindingWhiteList') == null
    }

    void "make sure binding trait and DomainClass did not get compiled in"() {
        // we dont want dependencies that have web-databinding as it adds properties overides into domain
        // and we dont want that. Also dont want DomainClass to get added as these both cause issues when used
        // without the entire grails infrastucture
        expect:
        ! WebDataBinding.isAssignableFrom(AppUser)
        ! DomainClass.isAssignableFrom(AppUser)
    }

}
