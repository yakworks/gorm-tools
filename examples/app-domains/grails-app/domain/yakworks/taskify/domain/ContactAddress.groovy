package yakworks.taskify.domain

class ContactAddress {
    static belongsTo = [contact: Contact]
    String street
    String city
    String state
    String postalCode
    String country

    static constraints = {
        street nullable: false
    }
}
