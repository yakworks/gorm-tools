package yakworks.taskify.domain

import java.time.LocalDate
import javax.persistence.Transient

import gorm.tools.rest.RestApi
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

import static grails.gorm.hibernate.mapping.MappingBuilder.*

//import gorm.restapi.RestApiController
/**
 * __This is test docs__
 * A task is an activity that **needs** to be accomplished within a defined period of time or
 * by a deadline to work towards work-related goals.
 */
@GrailsCompileStatic
@Entity
class Task {
    //ordering of contraints flow through to json-schema and the fields plugin for automatic scaffolding
    //its not required to add fields to constraints, they just need to be here if a specifc order is needed
    //if they should not be shown then add with display:false
    static constraints = {
        name description: "The task summary/description", example: "Design App",
                nullable: false, maxSize: 100
        //see https://github.com/json-schema-form/angular-schema-form/blob/master/docs/index.md#form-defaults-in-schema
        project description: "The project for this task",
                nullable: false, widget: 'picklist'

        type description: "The type of Task",
                nullable: false

        note maxSize: 1000,
                widget: '{"type": "textarea","placeholder": "Don\'t hold back.\nEnter a note."}'

        completed nullable: false

        dueDate description: "The day this task is due"

        reminderEmail description: "Email will be used for evil.",
                email: true,
                maxSize: 50

        billable nullable: false
        estimatedHours title: 'Est. Hours', scale: 2
        estimatedCost title: 'Est. Cost', format: 'money', scale: 2
        progressPct title: 'Progress', format: 'percent', max: 1.0, min: 0.0, scale: 2

        roleVisibility description: "What user roles are able to see this task",
                editable: true,
                widget: '{"type": "select","titleMap": "??some indicator of the source for the json"}'

        flex description: "Extra user fields"
        fooHide display: false //hide it from Fields and json-schema by default

        //dateCreated() //these default to editable=false
        //lastUpdated()
        //TODO for percentages see https://stackoverflow.com/questions/40673814/how-to-define-property-of-type-percentage-in-json-schema-4

    }
    //see https://github.com/grails/grails-data-mapping/blob/master/grails-datastore-gorm-validation/src/main/groovy/org/grails/datastore/gorm/validation/constraints/EmailConstraint.java
    //for adding new constraints

    static mapping = orm {
        comment "A task is an activity that needs to be accomplished within a defined period of time or by a deadline to work towards work-related goals."
        property('completed') { defaultValue: '0'}
        property('billable') { defaultValue: '0'}
        property('type') { defaultValue: 'test'}
    }

    String name
    Project project
    TaskType type = TaskType.Todo
    String note
    Boolean completed = false
    LocalDate dueDate
    String reminderEmail

    Boolean billable = false
    Long estimatedHours
    BigDecimal estimatedCost
    BigDecimal progressPct

    TaskFlex flex
    Date dateCreated
    Date lastUpdated

    @Transient
    String roleVisibility

    String getRoleVisibility() {
        'admin'
    }
    //void setRoleVisibility( String blah) {}

    String fooHide
    @Transient
    String getFooHide() {
        'bar'
    }

    //void setFooHide( String blah) {}

}

enum TaskType {
    Todo, Call, Meeting, Review, Development
}
