package yakworks.security.gorm

import spock.lang.Specification
import yakworks.security.PasswordConfig
import yakworks.security.gorm.model.SecPasswordHistory
import yakworks.security.gorm.model.SecPasswordHistoryRepo
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

import javax.inject.Inject

class SecPasswordHistorySpec extends Specification implements GormHibernateTest, SecurityTest {
    static List entityClasses = [SecPasswordHistory]
    static List springBeans = [PasswordConfig]

    @Inject SecPasswordHistoryRepo repo
    @Inject PasswordConfig passwordConfig

    void setup() {
        passwordConfig.historyEnabled = true
    }

    void "sanity check"() {
        expect:
        repo
        passwordConfig
    }

    void "create"() {
        when:
        repo.create(1L, "test")
        flush()

        then:
        noExceptionThrown()
        repo.findAllByUser(1L).size() == 1
    }

    void "older are deleted from history"() {
        setup:
        passwordConfig.historyLength = 3

        when:
        repo.create(1L, "test1")
        repo.create(1L, "test2")
        repo.create(1L, "test3")
        flush()

        then:
        repo.findAllByUser(1L).size() == 3

        when: "Adding 4th should remove 1st"
        repo.create(1L, "test4")
        flush()

        then:
        repo.findAllByUser(1L).size() == 3

        and:
        repo.findAllByUser(1L).password == ['test2', 'test3', 'test4']
    }
}
