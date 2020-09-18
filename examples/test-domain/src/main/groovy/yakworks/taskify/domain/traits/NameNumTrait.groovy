package yakworks.taskify.domain.traits

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

@CompileStatic
trait NameNumTrait {

    String num
    String name

    static List qSearchIncludes = ['num', 'name'] // quick search includes
    static List picklistIncludes = ['id', 'num', 'name'] //for picklist

    @CompileDynamic
    static NameNumTraitConstraints(Object delegate) {
        def c = {
            num description: "unique alpha-numeric identifier for this entity",
                nullable: false, blank: false, maxSize: 50
            name description: "the full name of this entity",
                nullable: false, blank: false, maxSize: 50
        }
        c.delegate = delegate
        c()
    }
}
