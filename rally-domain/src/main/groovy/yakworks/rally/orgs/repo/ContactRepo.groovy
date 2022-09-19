/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.repo

import groovy.transform.CompileStatic

import org.springframework.dao.DataRetrievalFailureException

import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.mango.api.QueryArgs
import gorm.tools.repository.GormRepository
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.events.AfterBindEvent
import gorm.tools.repository.events.AfterPersistEvent
import gorm.tools.repository.events.BeforeRemoveEvent
import gorm.tools.repository.events.RepoListener
import gorm.tools.repository.model.LongIdGormRepo
import gorm.tools.utils.GormUtils
import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import yakworks.api.problem.data.DataProblemCodes
import yakworks.commons.map.Maps
import yakworks.rally.activity.model.ActivityContact
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.ContactEmail
import yakworks.rally.orgs.model.ContactFlex
import yakworks.rally.orgs.model.ContactPhone
import yakworks.rally.orgs.model.ContactSource
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.tag.model.TagLink
import yakworks.security.gorm.model.AppUser

@GormRepository
@CompileStatic
class ContactRepo extends LongIdGormRepo<Contact> {

    List<String> toOneAssociations = ['flex']

    @RepoListener
    void beforeValidate(Contact contact) {
        setupNameProps(contact)
    }

    @RepoListener
    void beforeRemove(Contact contact, BeforeRemoveEvent e) {
        AppUser user = contact.user
        String hasRefName
        if (user) {
            hasRefName = 'User'
        }
        if (Org.query(contact: contact).count()) {
            hasRefName = 'Org primary contact'
        }
        if (ActivityContact.repo.count(contact)) {
            hasRefName = 'ActivityContact'
        }
        if(hasRefName){
            Map refArgs = [stamp: "Contact: ${contact.name}, id: ${contact.id}", other: hasRefName]
            throw DataProblemCodes.ReferenceKey.withArgs(refArgs).toException()
        }
        //remove
        TagLink.remove(contact)

        // XXX why are we keeping the locations around?
        // if its a location for a contact it should be deleted along with the contact right?
        Location.executeUpdate("update Location set contact = null where contact = :contact", [contact: contact]) //set contact to null

        // XXX we are not deleting Location or CSource? Why
        // something like this should be run no?
        // Location.query(contact: contact).deleteAll()
        // ContactSource.query(contact: contact).deleteAll()
    }

    /** lookup by num or ContactSource */
    @Override
    Contact lookup(Map data) {
        Contact contact
        if (data == null) data = [:] //if null then make it empty map so it can cycle down and blow error

        String sourceId = Maps.value(data, 'sourceId')
        if(sourceId) {
            List contactForSourceId = ContactSource.findAllWhere(sourceId: sourceId)
            contact = contactForSourceId[0].contact
        } else if (data.num) {
            String num = Maps.value(data, 'num')
            List contactForNum = Contact.findAllWhere(num:num)
            if(contactForNum?.size() == 1) {
                contact = contactForNum[0]
            } else if (contactForNum.size() > 1){
                throw new DataRetrievalFailureException("Multiple Contacts found for num: ${data.num}, lookup key must return a unique Contact")
            }
        }
        return load(contact?.getId())
    }

    @RepoListener
    void afterBind(Contact contact, Map data, AfterBindEvent e) {
        assignOrg(contact, data)
    }

    @RepoListener
    void afterPersist(Contact contact, AfterPersistEvent e) {
        if (contact.location?.isDirty()) contact.location.persist()
        syncChangesToUser(contact)
    }

    /**
     * Called from doAfterPersist and before afterPersist event
     * if its had a bind action (create or update) and it has data
     * creates or updates One-to-Many associations for this entity.
     */
    @Override
    void doAfterPersistWithData(Contact contact, PersistArgs args) {
        Map data = args.data

        if(data.locations) super.persistToManyData(contact, Location.repo, data.locations as List<Map>, "contact")
        if(data.phones) super.persistToManyData(contact, ContactPhone.repo, data.phones as List<Map>, "contact")
        if(data.emails) super.persistToManyData(contact, ContactEmail.repo, data.emails as List<Map>, "contact")
        if(data.sources) super.persistToManyData(contact, ContactSource.repo, data.sources as List<Map>, "contact")
        if(data.tags) TagLink.addOrRemoveTags(contact, data.tags)
    }

    @Override
    MangoDetachedCriteria<Contact> query(QueryArgs queryArgs, @DelegatesTo(MangoDetachedCriteria)Closure closure) {
        Map criteriaMap = queryArgs.criteria
        //if its has tags keys then this returns something to add to exists, will remove the keys as well
        DetachedCriteria tagExistsCrit = TagLink.getExistsCriteria(criteriaMap, Contact, 'contact_.id')

        MangoDetachedCriteria<Contact> detCrit = getMangoQuery().query(Contact, queryArgs, closure)
        //if it has tags key
        if(tagExistsCrit != null) {
            detCrit.exists(tagExistsCrit.id())
        }
        return detCrit
    }

    void removeAll(Org org) {
        gormStaticApi().executeUpdate 'DELETE FROM Contact WHERE org=:org', [org: org]
    }

    /**
     * if email or name is changed the progate them to user
     */
    void syncChangesToUser(Contact contact){
        AppUser user = contact.user
        if(user){
            if(contact.isDirty('email')){
                user.email = contact.email
            }
            if(contact.isDirty('name') && contact.user.name != contact.name){
                user.name = contact.name
            }
            if(user.isDirty()) user.persist()
        }
    }

    void setupNameProps(Contact c) {
        if(c.isNew()) {
            if(!c.firstName && c.name) c.firstName = c.name
        }
        concatName(c)
    }

    void concatName(Contact c) {
        String fullName = ((c.firstName ?: "") + ' ' + (c.lastName ?: "")).trim()
        c.name = fullName.size() > 50 ? fullName[0..49] : fullName
    }

    void assignOrg(Contact contact, Map data) {
        // data.orgId wins if its set, only do lookup if its not set
        if (!data.orgId) {
            if (data.org && data.org instanceof Map) {
                contact.orgId = Org.repo.findWithData(data.org as Map)?.getId()
            }
            else if(data.org && data.org instanceof Org){
                contact.orgId = ((Org)data.org).getId()
            }
        }
    }

    /*
    * build a User domain object from a contact if it does not exist.
    */
    @Transactional
    AppUser buildUserFromContact(Contact contact, String password) {
        if (!contact.user) {
            AppUser user = new AppUser(username: contact.email, email: contact.email)
            user.password = password //plain text password gets encrypted on persist to passHash
            user.id = contact.getId()
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
