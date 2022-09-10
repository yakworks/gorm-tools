package gorm.tools.model

import yakworks.testing.gorm.GormToolsHibernateSpec
import testing.CompositeNoVersion

class CompositeSpec extends GormToolsHibernateSpec {

    List<Class> getDomainClasses() { [CompositeNoVersion] }

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
