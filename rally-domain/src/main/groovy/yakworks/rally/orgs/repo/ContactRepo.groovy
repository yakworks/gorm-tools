/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.repo


import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataRetrievalFailureException

import gorm.tools.databinding.BindAction
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
    private static final String IS_PRIMARY = "isPrimary"

    @Autowired LocationRepo locationRepo
    @Autowired ContactSourceRepo contactSourceRepo

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

        TagLink.remove(contact)

        // ContactSource.query(contact: contact).deleteAll() - deleted with cascade as per domain mapping.
        Location.query(contact: contact).deleteAll()
        ContactSource.query(contactId: contact.id).deleteAll()

        //NOTE: This was here for CED but it was removed as logic is faulty to keep the location around for the contact if the contact is deleted.
        // I think the idea was to keep its location info even if contact was removed since contacts could be some kind of job.
        //Location.executeUpdate("update Location set contact = null where contact = :contact", [contact: contact]) //set contact to null
    }


    @Override
    void doBeforePersistWithData(Contact contact, PersistArgs args) {
        Map data = args.data
        if (args.bindAction == BindAction.Create) {
            setupSource(contact, data)
        }
        // we do primary location and contact here before persist so we persist org only once with contactId it is created
        if(data.location) createOrUpdateLocation(contact, data.location as Map)
    }

    /**
     * Create and setup source on contact
     */
    void setupSource(Contact contact, Map data) {
        if(!contact.id) contact.id = generateId()
        //the ContactSource will be persisted in createSource
        contact.source = ContactSource.repo.createSource(contact, data)
    }

    /**
     * lookup by num or ContactSource
     * This is called from findWithData and is used to locate contact for updates and associtaions
     */
    @Override
    Contact lookup(Map data) {
        Contact contact
        if (data == null) data = [:] //if null then make it empty map so it can cycle down and blow error

        String sourceId = data.sourceId

        //For convience, it allows specifying sourceId directly at top level along with other contact fields.
        if(data.source == null && sourceId) data.source = [sourceId: sourceId]

        if (data.source && data.source['sourceId']) {
            Long cid = contactSourceRepo.findContactIdBySourceId(Maps.value(data, "source.sourceId") as String)
            if(cid) return get(cid)
        }
        else if (data.num) {
            String num = data.num
            List contactForNum = Contact.findAllWhere(num: num)
            if(contactForNum?.size() == 1) {
                contact = contactForNum[0]
            } else if (contactForNum.size() > 1){
                throw new DataRetrievalFailureException("Multiple Contacts found for num: ${data.num}, lookup key must return a unique Contact")
            }
        }
        return contact
    }

    @RepoListener
    void afterBind(Contact contact, Map data, AfterBindEvent e) {
        assignOrg(contact, data)
    }

    @RepoListener
    void afterPersist(Contact contact, AfterPersistEvent e) {
        if (contact.location?.hasChanged()) contact.location.persist()
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
        if(data.getBoolean(IS_PRIMARY)) {
            Org org = Org.get(contact.orgId)
            org.contact = contact
            org.persist()
        }
        if(data.locations) super.persistToManyData(contact, Location.repo, data.locations as List<Map>, "contact")
        if(data.phones) super.persistToManyData(contact, ContactPhone.repo, data.phones as List<Map>, "contact")
        if(data.emails) super.persistToManyData(contact, ContactEmail.repo, data.emails as List<Map>, "contact")
        if(data.tags != null) TagLink.addOrRemoveTags(contact, data.tags)
    }

    @Override
    MangoDetachedCriteria<Contact> query(QueryArgs queryArgs, @DelegatesTo(MangoDetachedCriteria)Closure closure) {
        Map criteriaMap = queryArgs.qCriteria
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
            if(contact.hasChanged('email')){
                user.email = contact.email
            }
            if(contact.hasChanged('name') && contact.user.name != contact.name){
                user.name = contact.name
            }
            if(user.hasChanged()) user.persist()
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

    Location createOrUpdateLocation(Contact contact, Map data){
        if(!data) return
        //make sure params has org key
        data.orgId = contact.orgId
        data.contact = contact
        // if it had an op of remove then will return null and this set primary location to null
        contact.location = locationRepo.upsert(data).entity
        return contact.location
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

        //generate id if not already done, ContactSource etc will need it
        if(!toContat.id) toContat.id = Contact.repo.generateId()
        GormUtils.copyDomain(toContat, from)
        toContat.flex = GormUtils.copyDomain(ContactFlex, ContactFlex.get(from.flexId as Long), [contact: toContat])

        if(from.phones) {
            from.phones.each { ContactPhone p ->
                toContat.addToPhones(GormUtils.copyDomain(ContactPhone, p, [contact: toContat]))
            }
        }

        if(from.emails) {
            from.emails.each { ContactEmail e ->
                toContat.addToEmails(GormUtils.copyDomain(ContactEmail, e, [contact: toContat]))
            }
        }

        /* XXX contactSource.sourceId has unique key, can not copy it. What should happen @JB @JD
        if(from.source) {
            ContactSource source = GormUtils.copyDomain(ContactSource, from.source, [contactId:toContat.id])
            source.persist()
            toContat.source = source
        }*/
        if(from.locations) {
            from.locations.each { Location l ->
                Location c = GormUtils.copyDomain(Location, l, [org: toContat.org, contact: toContat])
                c.persist()
            }
        }

        return toContat.persist()
    }

}
