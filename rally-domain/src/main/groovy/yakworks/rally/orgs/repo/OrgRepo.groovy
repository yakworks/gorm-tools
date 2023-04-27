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
import yakworks.commons.beans.Transform
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
        //verifyCompany(org)
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
        List critTags = queryArgs.qCriteria.remove('tags') as List
        List critTagIds = queryArgs.qCriteria.remove('tagIds') as List

        DetachedCriteria<Org> detCrit = getMangoQuery().query(Org, queryArgs, closure)

        //if it has tags key
        if(critTags){
            //convert to id long list
            List<Long> tagIds = Transform.objectToLongList(critTags)
            detCrit.exists(OrgTag.buildExistsCriteria(tagIds))
        } else if(critTagIds) {
            //should be list of id if this key is present
            detCrit.exists(OrgTag.buildExistsCriteria(critTagIds))
        }
        return detCrit
    }
}
