package testing

import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity @GrailsCompileStatic
class Project {

    String name
    String description
    boolean isActive
    Nested nested
    Date testDate

    static constraints = {
        name        nullable: false, example: 'project name'
        description nullable: true,  example: 'project description'
        nested nullable: false
        testDate nullable: false, example: "2017-01-01"
        isActive nullable: false, example: 'false'
    }
}
