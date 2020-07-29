package taskify

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class TaskFlex {
    static belongsTo = [task: Task]

    String text1
    Date date1
    BigDecimal num1

    static mapping = {
        id generator: 'foreign', params: [property: 'task']
    }

}
