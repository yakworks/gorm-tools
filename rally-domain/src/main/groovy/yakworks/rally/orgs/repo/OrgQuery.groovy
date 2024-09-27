/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.repo

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

import gorm.tools.mango.DefaultQueryService
import gorm.tools.mango.MangoDetachedCriteria
import yakworks.rally.orgs.model.Org

@Service @Lazy
@CompileStatic
@Slf4j
class OrgQuery extends DefaultQueryService<Org> {

    @Autowired OrgTagRepo orgTagRepo

    OrgQuery() {
        super(Org)
    }

    /**
     * special handling for tags
     */
    // @Override
    // MangoDetachedCriteria<Org> query(QueryArgs queryArgs, @DelegatesTo(MangoDetachedCriteria)Closure closure = null) {
    //     //var crit = getMangoQuery().query(Org, queryArgs, closure)
    //     orgTagRepo.doExistsCriteria(queryArgs.qCriteria)
    //     return query(Org, queryArgs, closure)
    // }

    // @Override
    // protected MangoDetachedCriteria<Org> createCriteria(Class<Org> clazz, QueryArgs qargs, Closure applyClosure){
    //     orgTagRepo.doExistsCriteria(qargs.qCriteria)
    //     return super.createCriteria(Org, qargs, applyClosure)
    // }

    /**
     * override to add tags.
     * This runs after the tidyMap has been run which puts it into a standard format
     */
    @Override
    void applyCriteria(MangoDetachedCriteria<Org> mangoCriteria){
        orgTagRepo.doExistsCriteria(mangoCriteria.criteriaMap)
        super.applyCriteria(mangoCriteria)
    }
}
