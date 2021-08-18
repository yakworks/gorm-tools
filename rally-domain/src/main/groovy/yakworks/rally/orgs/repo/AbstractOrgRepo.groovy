/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.repo

import javax.annotation.Nullable
import javax.inject.Inject

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepo
import gorm.tools.repository.errors.EntityValidationException
import gorm.tools.repository.events.AfterBindEvent
import gorm.tools.repository.events.AfterPersistEvent
import gorm.tools.repository.events.AfterRemoveEvent
import gorm.tools.repository.events.BeforeBindEvent
import gorm.tools.repository.events.BeforeRemoveEvent
import gorm.tools.repository.events.RepoListener
import gorm.tools.repository.model.IdGeneratorRepo
import gorm.tools.support.MsgKey
import yakworks.commons.lang.Validate
import yakworks.rally.common.SourceType
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.model.OrgType

/**
 * base or OrgRepo. common functionality refactored out so can be overriden in application.
 */
@CompileStatic
abstract class AbstractOrgRepo implements GormRepo<Org>, IdGeneratorRepo {

    //@Inject @Nullable //required false so they dont need to be setup in unit tests
    LocationRepo locationRepo

    //@Inject @Nullable
    ContactRepo contactRepo

    //@Inject @Nullable
    OrgTagRepo orgTagRepo

    @RepoListener
    void beforeValidate(Org org) {
        if(org.isNew()) {
            Validate.notNull(org.type, "[org.type]")
            //Validate.notNull(org.type.typeSetup, "org.type.typeSetup")
            generateId(org)
        }
        wireAssociations(org)
    }

    /**
     * set up before a bind
     */
    @RepoListener
    void beforeBind(Org org, Map data, BeforeBindEvent be) {
        if (be.isBindCreate()) {
            org.type = getOrgTypeFromData(data)
            if(data.id) org.id = data.id as Long
            generateId(org)
        }
    }

    @RepoListener
    void afterBind(Org org, Map data, AfterBindEvent be) {
        if (be.isBindCreate()) {
            verifyNumAndOrgSource(org, data)
        }
        //do location
        if(data.location) createOrUpdatePrimaryLocation(org, data.location as Map)
        // do contact, support keyContact for legacy and Customers
        def contactData = data.contact ?: data.keyContact
        if(contactData) createOrUpdatePrimaryContact(org, contactData as Map)
        List contacts = data.remove("contacts")
        if(contacts) createOrUpdateContacts(org, contacts)
    }

    /**
     * An event handler, which is executed after persist operation on an Org entity.
     */
    @RepoListener
    void afterPersist(Org org, AfterPersistEvent e) {
        if (e.bindAction && e.data){
            Map data = e.data
            // do locations collection if it exists
            if(data.locations) doLocations(org, data.locations as List<Map>)
        }
        if(org.location?.isDirty()) org.location.persist()
        if(org.contact?.isDirty()) org.contact.persist()
    }

    @RepoListener
    void beforeRemove(Org org, BeforeRemoveEvent e) {
        if (org.source?.sourceType == SourceType.ERP) { //might be more in future
            def msgKey = new MsgKey("delete.error.source.external", ["Org ${org.name}", SourceType.ERP], "Org delete error")
            throw new EntityValidationException(msgKey, org)
        }
        orgTagRepo.removeAll(org)
    }

    /**
     * deletes org and all associated persons or users but only if is doesn't have invoices
     *
     * @param org the org domain object
     * @throws EntityValidationException if a spring DataIntegrityViolationException is thrown
     */
    @RepoListener
    void afterRemove(Org org, AfterRemoveEvent e) {
        Location.query(org: org).deleteAll()
        Contact.query(org: org).deleteAll()
        OrgSource.query(org: org).deleteAll()
    }

    /**
     * verifies source and creates one using num if it doesn't exist. Add validation error if num is null
     * called afterBind
     */
    boolean verifyNumAndOrgSource(Org org, Map data){
        // if no org num then let it fall through and fail validation
        if(!org.num) return false

        org.source = OrgSource.repo.createSource(org, data)
        org.source.persist()
    }


    void createOrUpdateContacts(Org org, List<Map> contacts) {
        for (Map row : contacts) {
            createOrUpdateContact(org, row)
        }
    }

    private Contact createOrUpdateContact(Org org, Map row) {
        row.org = org
        return contactRepo.createOrUpdate(row)
    }


    Contact createOrUpdatePrimaryContact(Org org, Map data){
        if(!data) return //exit fast if no data

        // if org has a contact then its and update or replace
        if(org.contact) {
            //if data has id
            Long cid = data.id as Long
            //if data has id and its different then replace
            if(cid && org.contact.id != cid) {
                org.contact = Contact.get(cid)
                return org.contact
            } else if(!cid) {
                data.id = org.contact.id
            }
        }
        //make sure it has the right settings
        data.isPrimary = true
        data.org = org
        org.contact = contactRepo.createOrUpdate(data)
        return org.contact
    }

    Location createOrUpdatePrimaryLocation(Org org, Map data){
        if(!data) return
        //make sure params has org key
        data.org = org
        // if it had an op of remove then will return null and this set primary location to null
        org.location = locationRepo.createOrUpdate(data)
        return org.location
    }

    void doLocations(Org org, List<Map> locations) {
        for(Map location: locations){
            location.org = org
            locationRepo.createOrUpdate(location)
        }
    }

    /**
     * gets the OrgType from data so we can do it early and outside of the binder.
     * the data params should either contain type, which can be the enum or a map with id
     * or orgTypeId key
     */
    OrgType getOrgTypeFromData(Map data) {
        if (data.type) {
            if (data.type instanceof Map) {
                //its should have an id, use that
                return OrgType.get((data.type as Map)['id'] as Long)
            } else if (data.type instanceof OrgType) {
                //assume its the enum
                return data.type as OrgType
            } else if (data.type instanceof String) { //this is only really used during tests but its here if needed
                return OrgType.valueOf(data.type as String) //must match exactly case sensitive
            }
        } else if (data.orgTypeId) {
            return OrgType.get(data.orgTypeId as Long)
        }
    }

    /**
     * makes sure the associations are wired to the org
     */
    void wireAssociations(Org org) {
        if (org.flex && !org.flex.id) org.flex.id = org.id
        if (org.info && !org.info.id) org.info.id = org.id
    }

    //util method that creates the OrgSource from num and assigns to the source record (if originator)
    OrgSource createSource(Org org, SourceType sourceType = SourceType.App) {
        OrgSource.repo.createSource(org, sourceType)
    }

}
