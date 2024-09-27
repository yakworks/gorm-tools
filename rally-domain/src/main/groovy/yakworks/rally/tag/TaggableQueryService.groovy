/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.tag


import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.mango.DefaultQueryService
import gorm.tools.mango.MangoDetachedCriteria
import yakworks.rally.tag.repo.TagLinkRepo

/**
 * Common base class for entity that has tags
 */
@CompileStatic
@Slf4j
class TaggableQueryService<D> extends DefaultQueryService<D> {

    @Autowired TagLinkRepo tagLinkRepo

    TaggableQueryService(Class<D> entityClass) {
        super(entityClass)
    }

    /**
     * override to add tags.
     * This runs after the tidyMap has been run which puts it into a standard format
     */
    @Override
    <D> void applyCriteria(MangoDetachedCriteria<D> mangoCriteria){
        Map crit = mangoCriteria.criteriaMap
        //if its has tags keys then this returns something to add to exists, will remove the keys as well
        tagLinkRepo.doExistsCriteria(crit, mangoCriteria.entityClass, "${mangoCriteria.alias}.id")
        super.applyCriteria(mangoCriteria)
    }

}
