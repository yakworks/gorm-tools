package yakworks.taskify.domain

import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

@IdEqualsHashCode
@GrailsCompileStatic
@Entity
class TaskFlex implements RepoEntity<TaskFlex>{
    static belongsTo = [task: Task]

    String text1
    Date date1
    BigDecimal num1

    static mapping = {
        id generator: 'foreign', params: [property: 'task']
    }

}
