package yakworks.taskify.domain.traits

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

@CompileStatic
trait NameDescriptionTrait {
    String name
    String description

    static List qSearchIncludes = ['name', 'description'] // quick search includes
    static List picklistIncludes = ['id', 'name'] //for picklist

    @CompileDynamic
    static NameDescriptionTraitConstraints(Object delegate) {
        def c = {
            description description: "the description for this entity",
                nullable: true, maxSize: 255
            name description: "the name of this entity",
                nullable: false, blank: false, maxSize: 50
        }
        c.delegate = delegate
        c()
    }
}
