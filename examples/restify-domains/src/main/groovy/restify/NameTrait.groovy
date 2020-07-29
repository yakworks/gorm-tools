package restify

import groovy.transform.CompileStatic

import grails.compiler.GrailsCompileStatic

@CompileStatic
trait NameTrait {
    String name
}

//@GrailsCompileStatic
class NameTraitConstraints implements NameTrait {

    static constraints = {
        name blank: false, nullable: false
    }
}
