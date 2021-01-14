package yakworks.taskify.domain

import gorm.tools.repository.model.RepoEntity
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

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
