package restify

import gorm.tools.rest.testing.RestApiFuncSpec
import gorm.tools.testing.TestDataJson
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import taskify.Task
import taskify.Project

@Integration(applicationClass = Application)
@Rollback
class TaskRestApiSpec extends RestApiFuncSpec {

    Class<Task> domainClass = Task

    String getResourcePath() {
        "${baseUrl}api/task"
    }

    Map getPostData() { TestDataJson.buildMap(Task, project: Project.get(1)) }

    Map putData = [name: "Task Update"]

    Map invalidData = ["name": null]

    List<String> getExcludes() {
        ['lastUpdated', 'dateCreated']
    }

}
