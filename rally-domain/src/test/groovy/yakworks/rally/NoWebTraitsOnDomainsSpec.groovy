package yakworks.rally

import org.springframework.util.ReflectionUtils

import yakworks.testing.gorm.unit.DomainRepoTest
import grails.artefact.DomainClass
import grails.web.databinding.WebDataBinding
import spock.lang.Specification
import yakworks.rally.orgs.model.Org

/**
 * This will fail if the org.grails:grails-plugin-controllers is in compile dependency during compile of gorm domain
 * we dont want this and there is no way to to turn it off
 * we don't want it as it adds a map constructor that uses the slow Grails binder and ties domains to controllers
 * we want the default groovy map constructor or ability to use the Groovy @MapConstrutor AST transformation
 */
class NoWebTraitsOnDomainsSpec extends Specification implements DomainRepoTest<Org> {

    void "check for instanceControllersDomainBindingApi"() {
        expect:
        // if it has instanceControllersDomainBindingApi then it means it got compiled with controller
        ReflectionUtils.findField(Org, 'instanceControllersDomainBindingApi') == null
        ReflectionUtils.findField(Org, '$defaultDatabindingWhiteList') == null
    }

    void "make sure binding trait and DomainClass did not get compiled in"() {
        // we dont want dependencies that have web-databinding as it adds properties overides into domain
        // and we dont want that. Also dont want DomainClass to get added as these both cause issues when used
        // without the entire grails infrastucture
        expect:
        ! WebDataBinding.isAssignableFrom(Org)
        ! DomainClass.isAssignableFrom(Org)
    }


}
