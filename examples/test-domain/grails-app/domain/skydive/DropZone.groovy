package skydive

import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class DropZone implements RepoEntity<DropZone>{
    String location

    static constraints = {
    }

    // boolean persistFoo(){
    //     foo()
    // }
}
