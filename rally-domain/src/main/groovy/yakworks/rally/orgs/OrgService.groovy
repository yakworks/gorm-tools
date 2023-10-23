/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs


import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

import yakworks.commons.lang.Validate
import yakworks.rally.config.OrgProps
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgMember
import yakworks.rally.orgs.model.OrgType

/**
 * event listener for afterbind to setup org member
 */
@Service @Lazy
@Slf4j
@CompileStatic
class OrgService {

    @Autowired(required = false)
    OrgDimensionService orgDimensionService

    @Autowired(required = false)
    OrgProps orgProps

    boolean isOrgMemberEnabled(){
        return orgProps?.members?.enabled
    }

    /** shortcut to config orgProps.partition */
    OrgProps.PartitionConfig getPartition(){
        orgProps.partition
    }

    String getPartitionPropName(){
        return orgProps.partition.type.propertyName
    }

    String getMemberPartitionPath(){
        return "member.${orgProps.partition.type.propertyName}"
    }

    String getMemberPartitionIdPath(){
        return "member.${orgProps.partition.type.propertyName}.id"
    }

    /**
     * Sets up OrgMember
     * Looks into org dimensions to find all parents for given orgtype and sets the parents accordingly
     *
     * @param org the org to setup the orgMember for
     * @param params should contain the id values for the required immediate parents.
     *        for example if the orgDimensionService has immediateParents of [Division,Sales] then
     *               map should contain [division: [id: 123], sales: [id: 234]]
     */
    void setupMember(Org org, Map params) {
        //EXIT FAST if not enabled
        if(!isOrgMemberEnabled()) return

        List<OrgType> immediateParents = orgDimensionService.getImmediateParents(org.type)
        //if it has parents so going to need to have a member too. kicks in validation in OrgMemberRepo
        if(immediateParents && !org.member) org.member = OrgMember.make(org)
        if(!params) return

        //spin through orgTypes for immediate parents and update parents
        for (OrgType type : immediateParents) {
            Map orgParam = params[type.propertyName]
            Validate.notEmpty(orgParam, "setupMember called but params does not contain ${type.propertyName}")
            //add type for lookup
            orgParam.type = type

            Org parent = Org.repo.findWithData(orgParam as Map)

            //if its has no parents then its a toplevel
            boolean isTopLevel = orgDimensionService.getImmediateParents(type).isEmpty()
            setupMember(org, parent, isTopLevel)
        }
        //make sure partition orgType is done
    }

    /**
     * Sets passed in org as a parent org for a given child org.
     * It will create a new OrgMember for the org if it doesn't exist.
     * then copies the member vals
     *
     * @param org  an Org entity to set parent to
     * @param parent the immediate parent org to copy member values from
     * @param isTopLevel if true then it wont require the parent org to have an orgMember as its the top level
     */
    void setupMember(Org org, Org parent, boolean isTopLevel) {
        if (!org.member) org.member = OrgMember.make(org) //create new orgmember
        if (org.companyId) org.member.company = Org.load(org.companyId)
        if (!isTopLevel) {
            Validate.notNull(parent.member, 'Parent org must have a member set at this point')
            ['branch', 'division', 'business', 'sales', 'region', 'factory', 'company'].each { String fld ->
                if (parent.member[fld]) org.member[fld] = parent.member[fld]
            }
            //for now company is special. this is temporary until we get rid of it or if not then settle on the business logic.
            if (!org.member.company) org.member.company = parent.member.company
        }
        String orgMemberName = parent.type.propertyName
        //if the parent is a customer its not in member so check hasProperty to make sure
        if (org.member.hasProperty(orgMemberName)) org.member[orgMemberName] = parent

    }

}
