package restify

import gorm.tools.rest.testing.RestApiFuncSpec
import gorm.tools.testing.TestDataJson
import grails.gorm.transactions.Transactional
import grails.testing.mixin.integration.Integration
import yakworks.taskify.domain.Project
import yakworks.taskify.domain.Task

// @Integration(applicationClass = Application)
@Integration
class TaskRestApiSpec extends RestApiFuncSpec {
    String path = "api/task"

    //@Transactional
    //Map getPostData() { return TestDataJson.buildMap(Task, project: Project.get(1)) }
    Map postData = [name: "task", project: [id: 1]]

    Map putData = [name: "Task Update"]

    Map invalidData = ["name": null]

    List<String> getExcludes() {
        ['lastUpdated', 'dateCreated']
    }
}
