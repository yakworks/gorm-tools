package gorm.tools.model

import groovy.transform.CompileStatic

import spock.lang.Specification
import testing.Cust
import yakworks.testing.gorm.unit.GormHibernateTest

class PersistableSpec extends Specification implements GormHibernateTest {

    static List entityClasses =[Cust]

    @CompileStatic
    void nullOutID(Cust o){
        o.id = null
    }

    def "Persistable"() {
        when:
        Cust orgNew = new Cust()
        Persistable persistable = orgNew as Persistable
        nullOutID(orgNew)

        then: "should be instance of Persistable and isNew"
        !orgNew.version
        orgNew instanceof Persistable
        persistable.isNew()
        orgNew.isNew()

        when:
        def org = build(Cust)
        Persistable persistable2 = org as Persistable

        then:
        org.id instanceof Long
        org.version != null
        org instanceof Persistable
        !org.isNew()
        !persistable2.isNew()
        persistable2.id == org.id
    }

}
