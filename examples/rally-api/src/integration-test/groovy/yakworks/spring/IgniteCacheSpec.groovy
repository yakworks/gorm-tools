package yakworks.spring

import org.apache.ignite.cache.spring.IgniteCacheManager
import org.springframework.beans.factory.annotation.Autowired

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.testing.gorm.integration.DomainIntTest

// Use as a simple to test when trying to see why application context has problem on init
@Integration
@Rollback
class IgniteCacheSpec extends Specification implements DomainIntTest {

    @Autowired IgniteCacheManager igniteCacheManager

    void "sanity check"() {
        when:
        assert igniteCacheManager
        igniteCacheManager.getCache("fooCache").put("foo", "bar")

        then:
        igniteCacheManager.getCache("fooCache").get("foo").get() == 'bar'
    }

}
