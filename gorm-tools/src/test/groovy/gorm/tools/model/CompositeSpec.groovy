package gorm.tools.model

import spock.lang.Specification
import testing.CompositeNoVersion
import yakworks.testing.gorm.unit.GormHibernateTest

class CompositeSpec extends Specification implements GormHibernateTest {

    static List entityClasses = [CompositeNoVersion]

    void "CompositeNoVersion test"() {
        when:
        def ent = new CompositeNoVersion(linkedId: 99, name: 'bar')

        then:
        !ent.id
        !ent.version
        ent.isNew()
        // ent.ident() == ent

        when:
        ent.persist()

        then:
        !ent.isNew()
        //gorm adds the id and version columns anyway but they are null
        !ent.id
        !ent.version

        when:
        flushAndClear()
        def ent2 = CompositeNoVersion.get(new CompositeNoVersion(linkedId: 99, name: 'bar'))

        then:
        !ent2.isNew()
        ent2.linkedId == 99
        //gorm adds the id and version columns anyway but they are null
        !ent.id
        !ent.version

    }

}
