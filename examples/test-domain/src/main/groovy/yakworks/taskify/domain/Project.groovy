package yakworks.taskify.domain

import java.time.LocalDate
import java.time.LocalDateTime

import gorm.tools.repository.model.RepoEntity
import gorm.tools.transform.IdEqualsHashCode
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@GrailsCompileStatic
@IdEqualsHashCode
//@RestApi(description = "This is a project ")
@Entity
class Project implements RepoEntity<Project>{
    static List qSearchIncludes = ['num', 'name']

    static constraints = {
        num description: "The project short code or unique identifier", example: "client-123",
                nullable: false, maxSize: 10
        name description: "The project name", example: "Cool Project",
                nullable: false, maxSize: 50
        inactive description: "is project inactivated", nullable: false, example: false

        billable description: "does this get invoiced? If its set to true, tasks can be overriden to be false ",
                nullable: false, example: true

        startDate description: "Start date of project.", example: "2017-01-01"
        endDate description: "End date of project.", example: "2017-12-30"
        activateDate description: "Date time project is activated", example: "2017-12-30"

    }

    static mapping = {
        inactive defaultValue: "0"
        billable defaultValue: "0"
    }

    String num
    String name
    String comments
    Boolean inactive = false
    Boolean billable = true
    LocalDate startDate
    LocalDate endDate
    LocalDateTime activateDate
}
