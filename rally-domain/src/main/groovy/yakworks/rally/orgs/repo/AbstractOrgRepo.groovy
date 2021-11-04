/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.repo

import groovy.transform.CompileStatic

import org.springframework.dao.DataRetrievalFailureException

import gorm.tools.model.SourceType
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
import yakworks.rally.orgs.OrgMemberService
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

    LocationRepo locationRepo

    ContactRepo contactRepo

    OrgTagRepo orgTagRepo

    OrgSourceRepo orgSourceRepo

    OrgMemberService orgMemberService

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
            if(data.member) orgMemberService.setupMember(org, data.remove('member') as Map)
        }

        // we do primary location and contact here before persist so we persist org only once with contactId it is created
        if(data.location) createOrUpdatePrimaryLocation(org, data.location as Map)
        // do contact, support keyContact for legacy and Customers
        def contactData = data.contact ?: data.keyContact
        if(contactData) createOrUpdatePrimaryContact(org, contactData as Map)

    }

    /**
     * An event handler, which is executed after persist operation on an Org entity.
     */
    @RepoListener
    void afterPersist(Org org, AfterPersistEvent e) {
        if (e.bindAction && e.data){
            Map data = e.data
            doAssociations(org, data)
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
        //remove tags
        orgTagRepo.remove(org)
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

    void doAssociations(Org org, Map data) {
        if(data.locations) persistAssociationData(org, Location.repo, data.locations as List<Map>, "org")
        if(data.contacts) persistAssociationData(org, Contact.repo, data.contacts as List<Map>, "org")
    }

    /**
     * gets the OrgType from data so we can do it early and outside of the binder.
     * the data params should either contain type, which can be the enum or a map with id
     * or orgTypeId key
     */
    OrgType getOrgTypeFromData(Map data) {
        if (data.type) {
            return coerceOrgType(data.type)
        } else if (data.orgTypeId) {
            return OrgType.get(data.orgTypeId as Long)
        }
    }

    OrgType coerceOrgType(Object orgTypeObj) {
        if(!orgTypeObj) return null

        if (orgTypeObj instanceof Map) {
            //its should have an id, use that
            return OrgType.get((orgTypeObj as Map)['id'] as Long)
        } else if (orgTypeObj instanceof OrgType) {
            //assume its the enum
            return orgTypeObj as OrgType
        } else if (orgTypeObj instanceof String) {
            //string should only really be used during tests but its here if needed
            return OrgType.findByName(orgTypeObj as String) //must match exactly case sensitive
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

    /**
     * Lookup Org by num or sourceId. Search by num is usually used for other orgs like division or num (non customer or custAccount)
     * where we have unique num. Search by sourceId is used when there is no org or org.id; for example to assign org on contact
     * @param data (num or source with sourceId and orgType)
     */
    //XXX this needs its own test
    @Override
    Org lookup(Map data) {
        Org org
        Long oid
        //if type is set then coerce to OrgType enum
        OrgType orgType = coerceOrgType(data.type)

        if (data.source && data.source['sourceId']) {
            Map source = data.source as Map
            if(!orgType && source.orgType) {
                orgType = OrgType.findByName(source.orgType as String)
            }

            if(orgType){
                oid = OrgSource.repo.findOrgIdBySourceIdAndOrgType(source.sourceId as String, orgType)
                if(oid) org = get(oid)
            }
            else {
                // lookup by just sourceId and see if it returns just one
                List<Long> res = OrgSource.repo.findOrgIdBySourceId(source.sourceId as String)
                if(res?.size() == 1) {
                    oid = res[0]
                } else if (res.size() > 1){
                    throw new DataRetrievalFailureException("Multiple Orgs found for sourceId: ${source.sourceId}, lookup key must return a unique Org")
                }
            }
            //TODO change this to load or have it be an argument
            org = get(oid)
        } else if(data.num)  {
            String num = data.num as String
            List orgsForNum = orgType ? Org.findAllWhere(num:num, orgType: orgType) : Org.findAllWhere(num:num)
            if(orgsForNum?.size() == 1) {
                org = orgsForNum[0]
            } else if (orgsForNum.size() > 1){
                throw new DataRetrievalFailureException("Multiple Orgs found for num: ${data.num}, lookup key must return a unique Org")
            }
        }
        return org
    }

}
