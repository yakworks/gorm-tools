package skydive

import gorm.tools.repository.RepoEntity
import grails.compiler.GrailsCompileStatic

@RepoEntity
@GrailsCompileStatic
class DropZone {
    String location

    static constraints = {
    }

    // boolean persistFoo(){
    //     foo()
    // }
}
