package yakworks.taskify.domain.traits

import groovy.transform.CompileStatic

@CompileStatic
trait NameNumTrait {

    String num
    String name

    static List qSearchIncludes = ['num', 'name'] // quick search includes
    static List pickListIncludes = ['id', 'num', 'name'] //for picklist
}

//@GrailsCompileStatic
class NameNumConstraints implements NameNumTrait {

    static constraints = {
        num blank: false, nullable: false, maxSize: 50
        name blank: false, nullable: false, maxSize: 50
    }
}
