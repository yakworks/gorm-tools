package yakworks.taskify.domain

import gorm.tools.repository.RepoEntity
import gorm.tools.transform.IdEqualsHashCode
import grails.persistence.Entity

@Entity @RepoEntity
@IdEqualsHashCode
class Customer implements Serializable {
    String name
    String num
    Location location
    String timezone

    static quickSearchFields = ["name", "num"]

    static constraints = {
        name nullable: false
    }
}
