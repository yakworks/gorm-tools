package gorm.tools.model

import gorm.tools.testing.hibernate.GormToolsHibernateSpec
import testing.Org

class PersistableSpec extends GormToolsHibernateSpec {

    List<Class> getDomainClasses() { [Org] }

    def "Persistable"() {
        when:
        Org orgNew = new Org()
        Persistable persistable = orgNew as Persistable<Long>

        then: "should be instance of Persistable and isNew"
        !orgNew.version
        orgNew instanceof Persistable
        persistable.isNew()
        orgNew.isNew()

        when:
        def org = build(Org)
        Persistable persistable2 = org as Persistable<Long>

        then:
        org.version != null
        org instanceof Persistable
        !org.isNew()
        !persistable2.isNew()
        persistable2.id == org.id
    }

}
