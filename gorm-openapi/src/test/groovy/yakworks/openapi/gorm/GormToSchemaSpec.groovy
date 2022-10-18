package yakworks.openapi.gorm

import spock.lang.Specification
import yakworks.rally.orgs.model.Org
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecRole
import yakworks.security.gorm.model.SecRoleUser
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

class GormToSchemaSpec extends Specification implements GormHibernateTest, SecurityTest {
    static entityClasses = [AppUser, SecRole, SecRoleUser, Org]

    GormToSchema gormToSchema = new GormToSchema()

    def "check Set string generic"() {
        given:
        Map schema = gormToSchema.generate(AppUser)

        expect:
        //role should be array of strings
        schema.properties.roles.items.type == 'string'
    }

    def "check composite keys"() {
        given:
        Map schema = gormToSchema.generate(SecRoleUser)

        expect:
        schema != null
        //schema['$schema'] == "http://json-schema.org/schema#"
        //schema.description == "This is a task"
        schema.type == "object"
    }


    def "sanity check Org read"() {
        given:
        Map schema = gormToSchema.generate(Org)

        expect:
        schema != null
        //schema['$schema'] == "http://json-schema.org/schema#"
        //schema.description == "This is a task"
        schema.type == "object"
        //schema.required.size() == 4
        //schema.required.containsAll(["name", "project", "note", "dueDate", "reminderEmail", "estimatedHours", "estimatedCost", "progressPct", "roleVisibility", "flex"])

        //verify properties
        def props = schema['properties']
        props != null
        //props.size() == 20 //14 props, + 6 id/version/createBy/date/editedBy/date
        props.size() == 18 //16 props, + 2 id/version  when audit is turned off

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

        //associations only id
        props.info != null
        props.info['$ref'] == 'OrgInfo.yaml'

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

    def "sanity check Org Create"() {
        given:
        Map schema = gormToSchema.generate(Org, ApiSchemaEntity.CruType.Create)

        expect:
        schema != null
        //schema['$schema'] == "http://json-schema.org/schema#"
        //schema.description == "This is a task"
        schema.type == "object"
        //schema.required.size() == 4
        //schema.required.containsAll(["name", "project", "note", "dueDate", "reminderEmail", "estimatedHours", "estimatedCost", "progressPct", "roleVisibility", "flex"])

        //verify properties
        def props = schema['properties']
        props != null
        //props.size() == 20 //14 props, + 6 id/version/createBy/date/editedBy/date
        props.size() == 15 //no id/ver and audit is turned off

    }

    // def "test generate attachments"() {
    //     given:
    //     def path = gormToSchema.generateYmlFile(Attachment)
    //
    //     expect:
    //     Files.exists(path)
    // }


    // def "test generate Activity"() {
    //     given:
    //     //def taggableVal = Activity.yakworks_rally_tag_model_Taggable__validation$get()
    //     //assert taggableVal instanceof Map
    //     def path = gormToSchema.generateYmlFile(Activity)
    //
    //     expect:
    //     Files.exists(path)
    // }
    //
    // def "test generateYmlModels"() {
    //     given:
    //     def path = gormToSchema.generateYmlFile(Org)
    //     gormToSchema.generateYmlModels()
    //
    //     expect:
    //     Files.exists(path)
    // }

}
