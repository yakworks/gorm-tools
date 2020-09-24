package yakworks.taskify.domain

import gorm.tools.transform.IdEqualsHashCode
import grails.persistence.Entity

@Entity
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
