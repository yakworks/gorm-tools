/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs

import javax.annotation.Nullable
import javax.inject.Inject

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

import yakworks.commons.lang.Validate
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgMember
import yakworks.rally.orgs.model.OrgType

/**
 * event listener for afterbind to setup org member
 */
@Service @Lazy
@Slf4j
@CompileStatic
class OrgMemberService {

    @Inject @Nullable
    OrgDimensionService orgDimensionService

    /**
     * Sets up OrgMember
     * Looks into org dimensions to find all parents for given orgtype and sets the parents accordingly
     *
     * @param org the org to setup the orgMember for
     * @param params should contain the id values for the required immediate parents.
     *               for example if the orgDimensionService has immediateParents of [Division,Sales] then
     *               map should contain [division: [id: 123], sales: [id: 234]]
     */
    void setupMember(Org org, Map params) {
        if(!orgDimensionService.orgMemberEnabled) return
        List<OrgType> immediateParents = orgDimensionService.getImmediateParents(org.type)
        //spin through orgTypes for immediate parents and update parents
        for (OrgType type : immediateParents) {
            Map orgParam = params[type.propertyName]
            Validate.notEmpty(orgParam, "setupMember called but params does not contain ${type.propertyName}")
            Org parent
            if(orgParam['id']) {
                parent = Org.get(orgParam['id'] as Long)
            } else {
                orgParam.type = type
                parent = Org.repo.lookup(orgParam as Map)
            }
            Validate.notNull(parent, "setupMember failed trying to get Org from param ${orgParam}}")

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
     * @param parent the imediate parent org to copy member values from
     * @param isTopLevel if true then it wont require the parent org to have an orgMember as its the top level
     */
    void setupMember(Org org, Org parent, boolean isTopLevel) {
        if (!org.member) org.member = OrgMember.make(org) //create new orgmember
        if(!isTopLevel) {
            Validate.notNull(parent.member, 'Parent org must have a member set at this point')
            ['branch', 'division', 'business', 'sales', 'region', 'factory'].each { String fld ->
                if (parent.member[fld]) org.member[fld] = parent.member[fld]
            }
        }
        String orgMemberName = parent.type.propertyName
        //if the parent is a customer or company its not in member so check hasProperty to make sure
        if(org.member.hasProperty(orgMemberName)) org.member[orgMemberName] = parent

    }

}
