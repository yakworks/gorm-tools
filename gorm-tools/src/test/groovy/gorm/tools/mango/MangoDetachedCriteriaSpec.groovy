package gorm.tools.mango


import gorm.tools.mango.jpql.JpqlQueryBuilder
import spock.lang.Specification
import testing.Cust
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.unit.GormHibernateTest

/**
 * Test for JPA builder
 */
class MangoDetachedCriteriaSpec extends Specification implements GormHibernateTest  {

    static List entityClasses = [KitchenSink]

    void "parse parseAlias"() {
        when:
        def crit = new MangoDetachedCriteria(KitchenSink)
        def prop = crit.parseAlias(" foo.x as  bar ", "")
        then:
        prop == "foo.x"
        crit.propertyAliases["_foo.x"] == "bar"

        when:
        prop = crit.parseAlias("foo.x", "")
        then:
        prop == "foo.x"
    }
}
