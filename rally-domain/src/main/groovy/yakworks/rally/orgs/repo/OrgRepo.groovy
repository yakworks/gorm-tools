/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.repo

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.Errors

import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.mango.api.QueryArgs
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.RepoListener
import yakworks.rally.orgs.model.Org

@GormRepository
@CompileStatic
@Slf4j
class OrgRepo extends AbstractOrgRepo {

    @Autowired OrgTagRepo orgTagRepo


    // add @Override
    @RepoListener
    void beforeValidate(Org org, Errors errors) {
        super.beforeValidate(org, errors)
        //dont try to setup member if it has any errors
        if(!errors.hasErrors()) wireOrgMember(org)
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
        orgTagRepo.doExistsCriteria(queryArgs.qCriteria)
        return getMangoQuery().query(Org, queryArgs, closure)
    }
}
