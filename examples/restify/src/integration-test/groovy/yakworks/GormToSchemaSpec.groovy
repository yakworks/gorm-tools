package yakworks

import java.nio.file.Files

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.openapi.GormToSchema
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.activity.model.Activity
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.orgs.model.Org

@Integration
@Rollback
class GormToSchemaSpec extends Specification {

    @Autowired
    GormToSchema gormToSchema

    //@Ignore
    def "sanity check Org"() {
        given:
        Map schema = jsonSchemaGenerator.generate(Org)

        expect:
        schema != null
        //schema['$schema'] == "http://json-schema.org/schema#"
        //schema.description == "This is a task"
        schema.type == "Object"
        //schema.required.size() == 4
        //schema.required.containsAll(["name", "project", "note", "dueDate", "reminderEmail", "estimatedHours", "estimatedCost", "progressPct", "roleVisibility", "flex"])

        //verify properties
        def props = schema['properties']
        props != null
        //props.size() == 20 //14 props, + 6 id/version/createBy/date/editedBy/date
        props.size() == 17 //15 props, + 2 id/version  when audit is turned off

        props.id != null
        props.id.type == 'integer'
        //props.id.format == "int64"
        props.id.readOnly == true

        props.version != null
        props.version.type == "integer"
        props.version.readOnly == true

        props.num != null
        props.num.type == "string"
        props.num.description == "Unique alpha-numeric identifier for this organization"
        props.num.example == "SPX-321"
        props.num.maxLength == 50

        props.name != null
        props.name.type == "string"
        props.name.description == "The full name for this organization"
        props.name.example == "SpaceX Corp."
        props.name.maxLength == 100

        //verify enum property
        props.type != null
        props.type.type == "string"
        (props.type.enum as List).containsAll([
            'Customer', 'CustAccount', 'Branch', 'Division', 'Business', 'Company', 'Prospect', 'Sales', 'Client', 'Factory', 'Region'
        ])
        props.type.enum.size() == 11
        //props.type.required == null
        //props.type.default == "Todo"

        //associations
        props.info != null
        props.info['$ref'] == 'OrgInfo.yaml'

        props.flex != null
        props.flex['$ref'] == "OrgFlex.yaml"

        //verify definitions
        // schema.definitions != null
        // schema.definitions.size() == 2
        // schema.definitions.TaskFlex != null
        //schema.definitions.TaskFlex.type == "Object"

    }

    def "test generate attachments"() {
        given:
        def path = jsonSchemaGenerator.generateYmlFile(Attachment)

        expect:
        Files.exists(path)
    }

    //@IgnoreRest
    def "test generate Activity"() {
        given:
        //def taggableVal = Activity.yakworks_rally_tag_model_Taggable__validation$get()
        //assert taggableVal instanceof Map
        def path = jsonSchemaGenerator.generateYmlFile(Activity)

        expect:
        Files.exists(path)
    }

    def "test generateYmlModels"() {
        given:
        def path = jsonSchemaGenerator.generateYmlFile(Org)
        jsonSchemaGenerator.generateYmlModels()

        expect:
        Files.exists(path)
    }

}
