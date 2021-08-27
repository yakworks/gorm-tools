package testing

import gorm.tools.job.JobRepoTrait
import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.BeforeBindEvent
import gorm.tools.repository.events.RepoListener
import groovy.transform.CompileStatic

@GormRepository
@CompileStatic
class JobImplRepo implements  JobRepoTrait<JobImpl> {

    // @RepoListener
    // void beforeBind(JobImpl jobImpl, Map data, BeforeBindEvent be) {
    //     if (be.isBindCreate()) {
    //         // org.type = getOrgTypeFromData(data)
    //         // if(data.id) org.id = data.id as Long
    //         // generateId(org)
    //     }
    // }

}
