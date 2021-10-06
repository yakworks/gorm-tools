/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.repo

import gorm.tools.repository.bulk.BulkableRepo
import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.errors.EntityValidationException
import gorm.tools.repository.events.AfterBindEvent
import gorm.tools.repository.events.AfterPersistEvent
import gorm.tools.repository.events.BeforePersistEvent
import gorm.tools.repository.events.BeforeRemoveEvent
import gorm.tools.repository.events.RepoListener
import gorm.tools.security.domain.AppUser
import gorm.tools.support.MsgKey
import gorm.tools.utils.GormUtils
import grails.gorm.transactions.Transactional
import yakworks.rally.activity.model.ActivityContact
import yakworks.rally.job.Job
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.ContactEmail
import yakworks.rally.orgs.model.ContactFlex
import yakworks.rally.orgs.model.ContactPhone
import yakworks.rally.orgs.model.ContactSource
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.tag.model.TagLink

@GormRepository
@CompileStatic
class ContactRepo implements GormRepo<Contact>, BulkableRepo<Contact, Job> {

    @RepoListener
    void beforeValidate(Contact contact) {
        if (contact.flex && !contact.flex.id) contact.flex.contact = contact
        contact.concatName()
    }

    @RepoListener
    void beforeRemove(Contact contact, BeforeRemoveEvent e) {
        AppUser user = contact.user
        if (user) {
            def msgKey = new MsgKey("delete.error.reference", ['Contact', contact.name, 'User'], "contact delete error")
            throw new EntityValidationException(msgKey, contact)
        }
        if (Org.query(contact: contact).count()) {
            def msgKey = new MsgKey("contact.not.deleted.iskey", [contact.name], "contact delete error")
            throw new EntityValidationException(msgKey, contact)
        }

        if (ActivityContact.repo.count(contact)) {
            def msgKey = new MsgKey("delete.error.reference",  ['Contact', contact.name, 'Activity'], "contact delete error")
            throw new EntityValidationException(msgKey, contact)
        }

        //remove
        TagLink.remove(contact)

        //XXX why are we keeping the locations around?
        // if its a location for a contact it should be deleted along with the contact right?
        Location.executeUpdate("update Location set contact = null where contact = :contact", [contact: contact]) //set contact to null

        //XXX we are not deleting Location or CSource? Why
        // something like this should be run no?
        // Location.query(contact: contact).deleteAll()
        // ContactSource.query(contact: contact).deleteAll()
    }


    @RepoListener
    void afterBind(Contact contact, Map data, AfterBindEvent e) {
        assignOrgFromOrgId(contact, data)
        contact.concatName()
        assignUserNameFromContactName(contact)
    }

    @RepoListener
    void beforePersist(Contact contact, BeforePersistEvent e) {
        if(contact.isDirty('email')) {
            AppUser user = contact.user
            if(user) {
                user.email = contact.email
                user.persist()
            }
        }
    }

    @RepoListener
    void afterPersist(Contact contact, AfterPersistEvent e) {
        if (e.bindAction && e.data){
            Map data = e.data
            doAssociations(contact, data)
        }
        if (contact.location?.isDirty()) contact.location.persist()
    }

    void doAssociations(Contact contact, Map data) {
        if(data.locations) persistAssociationData(contact, Location.repo, data.locations as List<Map>, "contact")
        if(data.phones) persistAssociationData(contact, ContactPhone.repo, data.phones as List<Map>, "contact")
        if(data.emails) persistAssociationData(contact, ContactEmail.repo, data.emails as List<Map>, "contact")
        if(data.sources) persistAssociationData(contact, ContactSource.repo, data.sources as List<Map>, "contact")
        if(data.tags) TagLink.addOrRemoveTags(contact, data.tags)
    }

    void assignOrgFromOrgId(Contact contact, Map data) {
        if (data['orgId'] && !data['org']) {
            Long orgId = data['orgId'] as Long
            contact.org = Org.get(orgId)
        }
    }

    @Transactional
    Contact assignUserNameFromContactName(Contact contact) {
        if (contact.user && contact.user.name != contact.name) {
            contact.user.name = contact.name
            contact.user.persist()
        }
        return contact
    }

    /*
    * build a User domain object from a contact if it does not exist.
    */
    @Transactional
    AppUser buildUserFromContact(Contact contact, String password) {
        if (!contact.user) {
            AppUser user = new AppUser(username: contact.email, email: contact.email)
            user.password = password //plain text password gets encrypted on persist to passHash
            user.id = contact.id
            contact.user = user
            user.persist()
        }
        return contact.user
    }

    @Transactional
    Contact copy( Contact from, Contact toContat) {
        if (from == null) return null

        GormUtils.copyDomain(toContat, from)
        toContat.flex = GormUtils.copyDomain(ContactFlex, ContactFlex.get(from.flexId as Long), [contact: toContat])

        from.phones.each { ContactPhone p ->
            toContat.addToPhones(GormUtils.copyDomain(ContactPhone, p, [contact: toContat]))
        }

        from.emails.each { ContactEmail e ->
            toContat.addToEmails(GormUtils.copyDomain(ContactEmail, e, [contact: toContat]))
        }

        from.sources.each { ContactSource s ->
            toContat.addToSources(GormUtils.copyDomain(ContactSource, s, [contact: toContat]))
        }
        from.locations.each { Location l ->
            Location c = GormUtils.copyDomain(Location, l, [org: toContat.org, contact: toContat])
            c.persist()
        }
        return toContat.persist()
    }


}
