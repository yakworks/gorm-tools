package yakworks.taskify.domain

import gorm.tools.repository.model.RepoEntity

class ContactAddress implements RepoEntity<ContactAddress> {
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
