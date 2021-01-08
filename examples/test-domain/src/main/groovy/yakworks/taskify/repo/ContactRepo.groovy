package yakworks.taskify.repo

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import gorm.tools.databinding.BindAction
import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.BeforeBindEvent
import gorm.tools.repository.events.RepoListener
import grails.gorm.transactions.Transactional
import yakworks.taskify.domain.Contact

@GormRepository
@CompileStatic
class ContactRepo implements GormRepo<Contact> {

    @CompileDynamic
    @RepoListener
    void beforeBind(Contact city, Map params, BeforeBindEvent ba) {
        String name = params.remove("name")
        if (name) {
            def (fname, lname) = name.split()
            params.firstName = fname
            params.lastName = lname
        }
    }

    @CompileStatic
    @Transactional
    Contact inactivate(Long id) {
        Contact contact = Contact.get(id)
        contact.inactive = true
        contact.persist()
        contact
    }
}
