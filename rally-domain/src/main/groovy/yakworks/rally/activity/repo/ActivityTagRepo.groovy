/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity.repo

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.model.Persistable
import gorm.tools.repository.GormRepository
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityTag
import yakworks.rally.common.LinkedEntityRepoTrait
import yakworks.rally.tag.model.Tag

@GormRepository
@Slf4j
@CompileStatic
class ActivityTagRepo implements LinkedEntityRepoTrait<ActivityTag, Tag> {

    @Override
    String getItemPropName() {'tag'}

    @Override
    Tag loadItem(Long id) { Tag.load(id)}

    /**
     * override in implementation to throw IllegalArgumentException if the tag.entityName does not match
     */
    @Override
    void validateCreate(Persistable entity, Tag tag){
        def entName = getEntityName(entity)
        if (!tag.isValidFor(entName))
            throw new IllegalArgumentException("Tag [${tag.name}] not valid for $entName, restricted with entityName:${tag.entityName}")
    }

    /**
     * Copies all tags from given Activity to target Activity
     */
    void copyToActivity(Activity from, Activity to) {
        List<Long> tags = listItemIds(from)
        if (tags) add(to, tags)
    }

    @Override
    MangoDetachedCriteria<ActivityTag> queryFor(Persistable entity){
        query(linkedId: entity.id)
    }

}
