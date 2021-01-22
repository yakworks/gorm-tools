package testing

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
    }

    boolean sampleRepoCall(){
        return true
    }

    boolean intstanceRepoCall(Org o){
        return true
    }
}
