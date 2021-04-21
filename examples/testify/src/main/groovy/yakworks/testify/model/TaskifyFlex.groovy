package yakworks.testify.model

import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

@IdEqualsHashCode
@GrailsCompileStatic
@Entity
class TaskifyFlex implements RepoEntity<TaskifyFlex>{
    static belongsTo = [task: Taskify]

    String text1
    Date date1
    BigDecimal num1

    static mapping = {
        id generator: 'foreign', params: [property: 'task']
    }

}
