package testing

import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity @GrailsCompileStatic
class Project {

    String name
    String description

    static constraints = {
        name        nullable: false, example: 'project name'
        description nullable: true,  example: 'project description'
    }
}
