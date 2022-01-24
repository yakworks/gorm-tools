/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.repo

import groovy.transform.CompileStatic

import org.springframework.validation.Errors

import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.mango.api.QueryArgs
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.RepoListener
import grails.gorm.DetachedCriteria
import yakworks.commons.lang.Transform
import yakworks.rally.orgs.model.Company
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgTag
import yakworks.rally.orgs.model.OrgType

@GormRepository
@CompileStatic
class OrgRepo extends AbstractOrgRepo {


    // add @Override
    @RepoListener
    void beforeValidate(Org org, Errors errors) {
        super.beforeValidate(org, errors)
        wireOrgMember(org)
        verifyCompany(org)
    }

    /**
     * makes sure org has a company on it, and sets it self if its a company
     */
    void verifyCompany(Org org){
        if (org.companyId == null) {
            if (org.type == OrgType.Company){
                org.companyId = org.id
            } else {
                org.companyId = Company.DEFAULT_COMPANY_ID
            }
        }
    }

    /**
     * Org member needs org set for validation
     */
    void wireOrgMember(Org org) {
        if (org.member && !org.member.id) {
            org.member.id = org.id
            org.member.org = org //needed for validation
        }
    }

    /**
     * special handling for tags
     */
    @Override
    MangoDetachedCriteria<Org> query(QueryArgs queryArgs, @DelegatesTo(MangoDetachedCriteria)Closure closure = null) {
        DetachedCriteria<Org> detCrit = getMangoQuery().query(Org, queryArgs, closure)
        Map criteriaMap = queryArgs.criteria
        //if it has tags key
        if(criteriaMap.tags){
            //convert to id long list
            List<Long> tagIds = Transform.objectToLongList(criteriaMap.tags as List)
            detCrit.exists(OrgTag.buildExistsCriteria(tagIds))
        } else if(criteriaMap.tagIds) {
            //should be list of id if this key is present
            detCrit.exists(OrgTag.buildExistsCriteria(criteriaMap.tagIds as List))
        }
        return detCrit
    }
}
