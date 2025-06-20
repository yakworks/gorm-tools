/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.repo

import javax.inject.Inject

import groovy.transform.CompileStatic

import org.springframework.dao.DataRetrievalFailureException
import org.springframework.validation.Errors

import gorm.tools.databinding.BindAction
import gorm.tools.model.Persistable
import gorm.tools.model.SourceType
import gorm.tools.problem.ValidationProblem
import gorm.tools.repository.GormRepo
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.events.AfterBindEvent
import gorm.tools.repository.events.AfterRemoveEvent
import gorm.tools.repository.events.BeforeBindEvent
import gorm.tools.repository.events.BeforeRemoveEvent
import gorm.tools.repository.events.RepoListener
import gorm.tools.repository.model.LongIdGormRepo
import gorm.tools.utils.GormMetaUtils
import gorm.tools.validation.Rejector
import yakworks.rally.config.OrgProps
import yakworks.rally.orgs.OrgService
import yakworks.rally.orgs.model.Company
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.model.OrgType

/**
 * base or OrgRepo. common functionality refactored out so can be overriden in application.
 */
@CompileStatic
abstract class AbstractOrgRepo extends LongIdGormRepo<Org> {

    @Inject LocationRepo locationRepo
    @Inject ContactRepo contactRepo
    @Inject OrgTagRepo orgTagRepo
    @Inject OrgSourceRepo orgSourceRepo
    @Inject OrgService orgService
    @Inject OrgProps orgProps
    @Inject PartitionOrgRepo partitionOrgRepo

    @RepoListener
    void beforeValidate(Org org, Errors errors) {
        if(org.isNew()) {
            Rejector.validateNotNull(org, errors, 'type')
        }
    }

    /**
     * makes sure org has a company on it, and sets it self if its a company
     */
    void ensureCompany(Org org){
        if (org.companyId == null) {
            if (org.type == OrgType.Company){
                org.companyId = org.id
            } else {
                org.companyId = Company.DEFAULT_COMPANY_ID
            }
        }
    }


    @RepoListener
    void beforeBind(Org org, Map data, BeforeBindEvent be) {
        if (be.isBindCreate()) {
            org.type = getOrgTypeFromData(data)

            //bind id early or generate one as we use it in afterBind
            if(data.id) {
                //dont depend on the args.bindId setting and always do it
                org.id = data.id as Long
            } else {
                generateId(org)
            }
        }
    }

    @RepoListener
    void afterBind(Org org, Map data, AfterBindEvent e) {
        if (e.isBindCreate()) {
            //ensureCompany in afterBind, as it needs to check companyId and orgType and id, by now all of em would have been set
            ensureCompany(org)
        }
    }

    @Override
    void doBeforePersist(Org org, PersistArgs args) {
        if(args.bindAction && args.data) {
            Map data = args.data
            if (args.bindAction == BindAction.Create) {
                verifyNumAndOrgSource(org, data)
                //if no orgType then let it fall through and fail validation, orgService is nullable so its easier to mock for testing
                if (org.type && orgService?.isOrgMemberEnabled())
                    orgService.setupMember(org, data.remove('member') as Map)
            }
            // we do primary location and contact here before persist so we persist org only once with contactId it is created
            if (data.location) createOrUpdatePrimaryLocation(org, data.location as Map)
            // do contact, support keyContact for legacy and Customers
            def contactData = data.contact ?: data.keyContact
            if (contactData) createOrUpdatePrimaryContact(org, contactData as Map)
        }
        partitionOrgCreateOrUpdate(org)
    }

    /**
     * do partitionOrg update here in beforePersist because after persist, the dirty state gets reset
     * and we can not check if name/num has changed
     */
    void partitionOrgCreateOrUpdate(Org org){
        //orgProps can't be null unless its unit testing, so allowing null makes it easier to mock up test data
        if (orgProps && org.isOrgType(orgProps.partition.type)) {
            partitionOrgRepo.createOrUpdate(org)
        }
    }

    /**
     * Called after persist
     * if its had a bind action (create or update) and it has data
     * creates or updates One-to-Many associations for this entity.
     */
    @Override
    void doAfterPersist(Org org, PersistArgs args) {
        if (args.bindAction && args.data) {
            Map data = args.data
            if (data.locations) persistToManyWithOrgId(org, Location.repo, data.locations as List<Map>)
            if (data.contacts) persistToManyWithOrgId(org, Contact.repo, data.contacts as List<Map>)
            if (data.tags != null) orgTagRepo.addOrRemove((Persistable) org, data.tags)
        }
    }

    @Override
    void persistToOneAssociations(Org org, List<String> associations){
        super.persistToOneAssociations(org, associations)
        if(GormMetaUtils.isNewOrDirty(org.location)) org.location.persist() //FIXME is this already validated?
        if(GormMetaUtils.isNewOrDirty(org.contact)) org.contact.persist()
    }

    /**
     * replaced persistToManyData to set the orgId instead of org.
     * persistToManyData sets the entity and we need to set the orgId instead.
     * so this replaces that functionality
     */
    void persistToManyWithOrgId(Org org, GormRepo assocRepo, List<Map> assocList){
        if(!assocList) return
        //remove, if there are any empty maps.
        assocList = assocList.findAll { Map it -> it.size() > 0}
        assocList.each { it['orgId'] = org.getId()}
        assocRepo.createOrUpdate(assocList)
    }

    /**
     * Checks if org can be deleted (sourceType != ERP)
     * And Deletes associated org domains
     */
    @RepoListener
    void beforeRemove(Org org, BeforeRemoveEvent e) {
        if (org.source?.sourceType == SourceType.ERP) { //might be more in future
            def args = [name: "Org: ${org.name}, source:${SourceType.ERP}"]
            throw ValidationProblem.of("error.delete.externalSource", args).entity(org).toException()
        }
        orgTagRepo.remove(org)
        contactRepo.removeAll(org)

        if (org.isOrgType(orgProps.partition.type)) {
            //pass PersistArgs, because default removeById(long) is disabled in PartitionOrgRepo to prevent deletion from API
            //As delete through api calls removeById(id)
            partitionOrgRepo.removeById(org.id, PersistArgs.defaults())
        }
    }

    /**
     * Deleted associated domains of org
     *
     * @param org the org domain object
     * @throws ValidationProblem.Exception if a spring DataIntegrityViolationException is thrown
     */
    @RepoListener
    void afterRemove(Org org, AfterRemoveEvent e) {
        Location.query(orgId: org.getId()).deleteAll()
        Contact.query(orgId: org.getId()).deleteAll()
        OrgSource.query(orgId: org.getId()).deleteAll()
    }

    /**
     * verifies source and creates one using num if it doesn't exist. Add validation error if num is null
     * called afterBind
     */
    boolean verifyNumAndOrgSource(Org org, Map data){
        // if no org num or orgType then let it fall through and fail validation
        if(!org.num || !org.type) return false

        org.source = OrgSource.repo.createSource(org, data)
        org.source.persist()
    }

    Contact createOrUpdatePrimaryContact(Org org, Map data){
        if(!data) return //exit fast if no data
        //just in case isPrimary was passed in then null it out so contact doesnt try to set it again.
        if(data.isPrimary) data.isPrimary = null
        // if org has a contact then its an update or replace
        if(org.contact) {
            //if data has id
            Long cid = data.id as Long
            //if data has id and its different then replace
            if(cid && org.contact.getId() != cid) {
                org.contact = Contact.load(cid)
                return org.contact
            } else if(!cid) {
                data.id = org.contact.getId()
            }
        }
        //contact is already being set as primary, remove if its there so it doesnt re update the org in contactRepo
        data.remove('isPrimary')

        data.orgId = org.getId()
        org.contact = contactRepo.upsert(data).entity
        return org.contact
    }

    Location createOrUpdatePrimaryLocation(Org org, Map data){
        if(!data) return
        //make sure params has org key
        data.orgId = org.getId()

        //if org already has a location, then set the existing location's id if not provided
        //so that upsert will update existing location and not try to create a new one.
        if(org.location && !data['id']) {
            data['id'] = org.location.id
        }

        // if it had an op of remove then will return null and this set primary location to null
        org.location = locationRepo.upsert(data).entity
        return org.location
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
            return OrgType.get(orgTypeObj) //must match exactly case sensitive
        }
    }

    /**
     * Used in testing, creates the OrgSource from num and assigns to the source record (if originator)
     */
    OrgSource createSource(Org org, SourceType sourceType = SourceType.App) {
        if(!org.id) generateId(org)
        OrgSource.repo.createSource(org, sourceType)
    }

    /**
     * Lookup Org by num or sourceId. Search by num is usually used for other orgs like division or num (non customer or custAccount)
     * where we have unique num. Search by sourceId is used when there is no org or org.id; for example to assign org on contact
     * NOTE: This is called from findWithData and is used to locate for updates and associations
     * SHOULD NOT NORMALLY BE CALLED DIRECTLY, findWithDatais used most of the time
     * @param data (num or source with sourceId and orgType)
     */
    /**
     * lookup by num or ContactSource
     * This is called from findWithData and is used to locate contact for updates and associtaions
     */
    @Override
    Org lookup(Map data) {
        Org org
        Long oid
        //if type is set then coerce to OrgType enum
        OrgType orgType = coerceOrgType(data.type)
        // special case for customer lookup when it comes with org.source; for example [org:[source:[sourceId:K14700]]
        if(data.org) {
            data.source = data.org['source']
            data.sourceId = data.org['sourceId']
        }
        //nest sourceId under source if pecified up one level.
        if(data.source == null && data.sourceId) data.source = [sourceId: data.sourceId]

        if (data.source && data.source['sourceId']) {
            Map source = data.source as Map
            if(!orgType && source.orgType) {
                orgType = OrgType.get(source.orgType)
            }

            if(orgType){
                oid = orgSourceRepo.findOrgIdBySourceIdAndOrgType(source.sourceId as String, orgType)
                if(oid) org = get(oid)
            }
            else {
                // lookup by just sourceId and see if it returns just one
                List<Long> res = orgSourceRepo.findOrgIdBySourceId(source.sourceId as String)
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
            List orgsForNum = orgType ? Org.findAllWhere(num:num, type: orgType) : Org.findAllWhere(num:num)
            if(orgsForNum?.size() == 1) {
                org = (Org)orgsForNum[0]
            } else if (orgsForNum.size() > 1){
                throw new DataRetrievalFailureException("Multiple Orgs found for num: ${data.num}, lookup key must return a unique Org")
            }
        }
        return org
    }

}
