/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.tag.repo

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import gorm.tools.model.Persistable
import gorm.tools.repository.GormRepository
import gorm.tools.repository.model.AbstractLinkedEntityRepo
import yakworks.rally.tag.model.Tag
import yakworks.rally.tag.model.TagLink

import static gorm.tools.utils.GormUtils.entityListToIdMap

@Slf4j
@GormRepository
@CompileStatic
class TagLinkRepo extends AbstractLinkedEntityRepo<TagLink, Tag> {

    TagLinkRepo(){
        super(Tag, 'tag')
    }

    /**
     * override in implementation to throw IllegalArgumentException if the tag.entityName does not match
     */
    @Override
    void validateCreate(Persistable entity, Tag tag){
        super.validateCreate(entity, tag)
        def entName = getLinkedEntityName(entity)
        if (!tag.isValidFor(entName))
            throw new IllegalArgumentException("Tag [${tag.name}] not valid for $entName, restricted with entityName:${tag.entityName}")
    }

    boolean hasTags(Persistable entity) {
        count(entity)
    }

    List<Tag> listTags(Persistable linkedEntity) {
        list(linkedEntity)*.tag
    }

    List<TagLink> addTags(Persistable linkedEntity, List<Tag> tags) {
        List<Map> ids = entityListToIdMap(tags)
        addOrRemove(linkedEntity, ids)
    }

    void copyTags(Persistable fromEntity, Persistable toEntity) {
        copyRelated(fromEntity, toEntity)
    }

}
