package yakworks.taskify.domain.traits

import groovy.transform.CompileStatic

@CompileStatic
trait NameDescriptionTrait {
    String name
    String description

    static List qSearchIncludes = ['name', 'description'] // quick search includes
    static List pickListIncludes = ['id', 'name'] //for picklist
}

//@GrailsCompileStatic
class NameDescriptionConstraints implements NameDescriptionTrait {

    static constraints = {
        description nullable: true, maxSize: 255
        name blank: false, nullable: false, maxSize: 50
    }
}
