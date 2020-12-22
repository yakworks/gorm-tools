package yakworks.taskify.domain

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.BeforePersistEvent
import gorm.tools.repository.events.RepoListener
import grails.gorm.transactions.Transactional

@GormRepository
@CompileStatic
class OrgRepo implements GormRepo<Org> {

    @RepoListener
    void beforeValidate(Org o) {
        o.beforeValidateCheck = "got it"
        //test rejectValue
        if(o.location?.street == 'OrgRepoStreet'){
            rejectValue(o, 'location.street', o.location.street, 'from.OrgRepo')
        }
        if(o.name == 'foos'){
            rejectValue(o, 'name', o.name, 'no.foos')
        }
    }

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
