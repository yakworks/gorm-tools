package yakworks.taskify.domain

import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@GrailsCompileStatic
@Entity
class TaskFlex {
    static belongsTo = [task: Task]

    String text1
    Date date1
    BigDecimal num1

    static mapping = {
        id generator: 'foreign', params: [property: 'task']
    }

}
