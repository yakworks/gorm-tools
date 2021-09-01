package yakworks.rally.job

import gorm.tools.databinding.BindAction
import gorm.tools.job.JobRepoTrait
import gorm.tools.json.Jsonify
import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.BeforeBindEvent
import gorm.tools.repository.events.RepoListener
import gorm.tools.source.SourceType
import groovy.transform.CompileStatic

@GormRepository
@CompileStatic
class JobRepo implements  JobRepoTrait<Job> {

    @RepoListener
    void beforeBind(Job job, Map data, BeforeBindEvent be) {
        if (be.isBindCreate()) {
            // must be Job called fro RestApi
            if (data.dataPayload) {
                // def res = Jsonify.render(data.dataPayload) //XXX java.lang.NoClassDefFoundError: grails.plugin.json.view.template.JsonViewTemplate
                // job.data = res.jsonText.bytes
                def bytes = data.dataPayload.toString().bytes // just for now till we figure out Jsonify issue
                job.data = bytes

                job.sourceType = SourceType.RestApi  // we should default to RestApi if dataPayload is passed
            }

        }
    }

}
