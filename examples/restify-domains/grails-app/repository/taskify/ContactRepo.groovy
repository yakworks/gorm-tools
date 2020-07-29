package taskify

import gorm.tools.databinding.BindAction
import gorm.tools.repository.GormRepo
import grails.gorm.transactions.Transactional

@Transactional
class ContactRepo implements GormRepo<Contact> {

    void beforeBind(Contact city, Map params, BindAction ba) {
        String name = params.remove("name")
        if (name) {
            def (fname, lname) = name.split()
            params.firstName = fname
            params.lastName = lname
        }
    }

    Contact inactivate(Long id) {
        Contact contact = Contact.get(id)
        contact.inactive = true
        contact.persist()
        contact
    }
}
