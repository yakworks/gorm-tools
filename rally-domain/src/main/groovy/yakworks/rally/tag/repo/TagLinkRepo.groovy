/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.tag.repo

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import gorm.tools.model.Persistable
import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.support.Results
import yakworks.rally.common.LinkXRefRepo
import yakworks.rally.tag.model.Tag
import yakworks.rally.tag.model.TagLink

@Slf4j
@GormRepository
@CompileStatic
class TagLinkRepo implements LinkXRefRepo<TagLink, Tag>, GormRepo<TagLink> {

    List<Tag> listTags(Persistable linkedEntity) {
        list(linkedEntity)*.tag
    }

    List<TagLink> addTags(Persistable linkedEntity, List<Tag> tags) {
        List<Long> ids = collectIds(tags)
        add(linkedEntity, ids)
    }

    @Override
    String getItemPropName() {'tag'}

    /**
     * override in implementation to throw IllegalArgumentException if the tag.entityName does not match
     */
    @Override
    void validateCreate(Persistable entity, Tag tag){
        def entName = getEntityName(entity)
        if (!tag.isValidFor(entName))
            throw new IllegalArgumentException("Tag [${tag.name}] not valid for $entName, restricted with entityName:${tag.entityName}")
    }

    @Override
    Tag loadItem(Long id) { Tag.load(id)}

    void copy(Persistable fromEntity, Persistable toEntity) {
        Results results = Results.OK
        List links = queryFor(fromEntity).list()
        for(TagLink tagLink : links){
            create(toEntity, tagLink.tag)
        }
    }

}
