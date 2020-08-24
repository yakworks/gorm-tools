package yakworks.taskify.domain

import groovy.transform.CompileStatic

import gorm.tools.compiler.GormRepository
import gorm.tools.repository.GormRepo
import gorm.tools.repository.events.BeforePersistEvent
import gorm.tools.repository.events.RepoListener
import grails.gorm.transactions.Transactional

@GormRepository
@CompileStatic
class OrgRepo implements GormRepo<Org> {


    @RepoListener
    void beforePersist(Org o, BeforePersistEvent e) {
        if(!o.type) o.type = OrgType.load(1)
        if(!o.kind) o.kind = Org.Kind.CLIENT
    }


    @Transactional
    Org inactivate(Long id) {
        Org o = Org.get(id)
        o.inactive = true
        o.persist()
        o
    }
}
