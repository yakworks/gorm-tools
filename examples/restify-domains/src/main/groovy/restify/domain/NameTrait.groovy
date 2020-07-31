package restify.domain

import groovy.transform.CompileStatic

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
