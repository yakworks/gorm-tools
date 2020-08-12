package yakworks.taskify.domain

import gorm.tools.databinding.BindAction
import gorm.tools.repository.GormRepo
import gorm.tools.repository.events.BeforeBindEvent
import gorm.tools.repository.events.RepoListener
import grails.gorm.transactions.Transactional

class ContactRepo implements GormRepo<Contact> {

    @RepoListener
    void beforeBind(Contact city, Map params, BeforeBindEvent ba) {
        String name = params.remove("name")
        if (name) {
            def (fname, lname) = name.split()
            params.firstName = fname
            params.lastName = lname
        }
    }

    @Transactional
    Contact inactivate(Long id) {
        Contact contact = Contact.get(id)
        contact.inactive = true
        contact.persist()
        contact
    }
}
