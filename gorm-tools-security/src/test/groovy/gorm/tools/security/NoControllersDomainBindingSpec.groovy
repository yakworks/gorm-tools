package gorm.tools.security

import org.springframework.util.ReflectionUtils

import gorm.tools.audit.StampedEntity
import gorm.tools.security.domain.AppUser
import gorm.tools.testing.unit.DomainRepoTest
import spock.lang.Specification

/**
 * This will fail if the org.grails:grails-plugin-controllers is in compile dependency during compile of gorm domain
 * we dont want this and there is no way to to turn it off
 * we don't want it as it adds a map constructor that uses the slow Grails binder and ties domains to controllers
 * we want the default groovy map constructor or ability to use the Groovy @MapConstrutor AST transformation
 */
class NoControllersDomainBindingSpec extends Specification implements DomainRepoTest<AppUser> {

    void "check for instanceControllersDomainBindingApi"() {
        expect:
        // if it has instanceControllersDomainBindingApi then it means it got compiled with controller
        ReflectionUtils.findField(AppUser, 'instanceControllersDomainBindingApi') == null
        ReflectionUtils.findField(AppUser, '$defaultDatabindingWhiteList') == null
        //as a sanity check the domain in the tests will have had the controller lib and so will have the fields
        ReflectionUtils.findField(StampedEntity, 'instanceControllersDomainBindingApi') != null
        ReflectionUtils.findField(StampedEntity, '$defaultDatabindingWhiteList') != null
    }

}
