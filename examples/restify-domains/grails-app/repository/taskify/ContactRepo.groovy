package taskify

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import gorm.tools.databinding.BindAction
import gorm.tools.repository.GormRepo
import grails.gorm.transactions.Transactional

@CompileStatic
class ContactRepo implements GormRepo<Contact> {

    @CompileDynamic
    void beforeBind(Contact city, Map params, BindAction ba) {
        String name = params.remove("name")
        if (name) {
            def (fname, lname) = name.split()
            // String[] names = name.split()
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
