package testing

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.RepoListener

@GormRepository
@CompileStatic
class CustRepo implements GormRepo<Cust> {

    @RepoListener
    void beforeValidate(Cust o) {
        o.beforeValidateCheck = "got it"
    }

    boolean sampleRepoCall(){
        return true
    }

    boolean intstanceRepoCall(Cust o){
        return true
    }
}
