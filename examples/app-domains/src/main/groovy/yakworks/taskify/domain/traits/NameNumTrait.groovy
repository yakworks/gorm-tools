package yakworks.taskify.domain.traits

import groovy.transform.CompileStatic

@CompileStatic
trait NameNumTrait {

    String num
    String name
}

//@GrailsCompileStatic
class NameNumConstraints implements NameNumTrait {

    static constraints = {
        num blank: false, nullable: false, maxSize: 50
        name blank: false, nullable: false, maxSize: 50
    }
}
