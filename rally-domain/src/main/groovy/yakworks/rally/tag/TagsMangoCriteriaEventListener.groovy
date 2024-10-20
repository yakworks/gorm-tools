/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.tag


import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.mango.MangoQueryCriteriaEvent
import yakworks.rally.tag.model.Taggable
import yakworks.rally.tag.repo.TagLinkRepo

@Component
@CompileStatic
class TagsMangoCriteriaEventListener implements ApplicationListener<MangoQueryCriteriaEvent> {

    @Autowired TagLinkRepo tagLinkRepo

    @Override
    void onApplicationEvent(MangoQueryCriteriaEvent event) {
        if(!Taggable.isAssignableFrom(event.entityClass)) return
        MangoDetachedCriteria mangoCriteria = event.mangoCriteria
        Map crit = mangoCriteria.criteriaMap
        //if its has tags keys then this returns something to add to exists, will remove the keys as well
        tagLinkRepo.doExistsCriteria(crit, mangoCriteria.entityClass, "${mangoCriteria.alias}.id")
    }

}
