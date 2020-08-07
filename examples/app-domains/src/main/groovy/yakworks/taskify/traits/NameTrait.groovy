package yakworks.taskify.traits

import groovy.transform.CompileStatic

@CompileStatic
trait NameTrait {
    String name
    String num
}

//@GrailsCompileStatic
class NameTraitConstraints implements NameTrait {

    static constraints = {
        name blank: false, nullable: false
    }
}
