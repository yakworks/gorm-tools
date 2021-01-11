package yakworks.taskify.domain

import gorm.tools.repository.model.RepoEntity
import gorm.tools.transform.IdEqualsHashCode
import grails.persistence.Entity

@Entity
@IdEqualsHashCode
class Customer implements RepoEntity<Customer>, Serializable {
    String name
    String num
    Location location
    String timezone

    static quickSearchFields = ["name", "num"]

    static constraints = {
        name nullable: false
    }
}
