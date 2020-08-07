package yakworks.taskify.domain

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
