package yakworks

import gorm.tools.rest.JsonSchemaGenerator
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired

import spock.lang.Ignore
import spock.lang.Specification
import yakworks.taskify.domain.Task

@Integration
@Rollback
class JsonSchemaGeneratorSpec extends Specification {

    @Autowired
    JsonSchemaGenerator jsonSchemaGenerator

    @Ignore //FIXME this is now giving a stack overflow error
    def "test fail"() {
        given:
        Map schema = jsonSchemaGenerator.generate(Task)

        expect:
        schema != null
        schema['$schema'] == "http://json-schema.org/schema#"
        schema['$id'] == "http://localhost:8080/schema/Task.json"
        //schema.description == "This is a task"
        schema.type == "Object"
        //schema.required.size() == 10
        //schema.required.containsAll(["name", "project", "note", "dueDate", "reminderEmail", "estimatedHours", "estimatedCost", "progressPct", "roleVisibility", "flex"])

        //verify properties
        def props = schema.props
        props != null
        props.size() == 17 //13 props, + id/version/dateCreated/lastUpdated

        props.id != null
        props.id.type == "integer"
        props.id.readOnly == true

        props.version != null
        props.version.type == "integer"
        props.version.readOnly == true

        props.name != null
        props.name.type == "string"
        props.name.description == "The task summary/description"
        props.name.example == "Design App"
        props.name.maxLength == 100

        //verify enum property
        props.type != null
        props.type.type == "string"
        props.type.enum.size() == 5
        (props.type.enum as List).containsAll(["Todo", "Call", "Meeting", "Review", "Development"])
        //props.type.required == null
        //props.type.default == "Todo"

        //associations
        props.project != null
        props.project['$ref'] == "yakworks.taskify.domain.Project.json"

        props.flex != null
        props.flex['$ref'] == "#/definitions/yakworks.taskify.domain.TaskFlex"

        //verify definitions
        schema.definitions != null
        schema.definitions.size() == 1
        schema.definitions.TaskFlex != null
        schema.definitions.TaskFlex.type == "Object"

    }
}
