/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.repo

import javax.annotation.Nullable
import javax.inject.Inject

import groovy.transform.CompileStatic

import org.springframework.validation.Errors

import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.RepoListener
import yakworks.rally.orgs.OrgDimensionService
import yakworks.rally.orgs.model.OrgMember
import yakworks.rally.orgs.model.OrgType

@GormRepository
@CompileStatic
class OrgMemberRepo implements GormRepo<OrgMember> {

    @Inject @Nullable
    OrgDimensionService orgDimensionService

    @RepoListener
    void beforeValidate(OrgMember orgMember, Errors errors) {
        validateMembers(orgMember, errors)
    }

    void validateMembers(OrgMember orgMember, Errors errors){
        // For OrgMember, All parents levels for the given orgtype is required
        OrgType memOrgType = orgMember.org.type
        List<OrgType> parents = orgDimensionService.getParentLevels(memOrgType)

        for(OrgType orgtype : parents){
            String propName = orgtype.propertyName

            //these fields wont be in the member so continue if its one of them
            List excludeTypes = ['customer', 'custAccount', 'company']
            if(excludeTypes.contains(propName)) continue

            if(orgMember[propName] == null){
                rejectNullValue(orgMember, propName, errors)
            }
        }
    }

}
