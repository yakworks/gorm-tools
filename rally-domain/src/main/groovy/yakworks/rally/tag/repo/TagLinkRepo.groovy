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
import grails.gorm.DetachedCriteria
import yakworks.commons.lang.Transform
import yakworks.rally.tag.model.Tag
import yakworks.rally.tag.model.TagLink

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
        List<Map> ids = Transform.objectListToIdMapList(tags)
        addOrRemove(linkedEntity, ids)
    }

    void copyTags(Persistable fromEntity, Persistable toEntity) {
        copyRelated(fromEntity, toEntity)
    }

    /**
     * build exists criteria for the linkedId and tag list
     */
    DetachedCriteria buildExistsCriteria(List tagList, Class linkedEntityClazz, String linkedIdJoinProperty){
        return TagLink.query {
            eqProperty("linkedId", linkedIdJoinProperty)
            eq("linkedEntity", getLinkedEntityName(linkedEntityClazz))
            inList('tag.id', Transform.toLongList(tagList))
        }
    }

    /**
     * Add exists criteria to a DetachedCriteria if its has tags
     * in the criteriaMap
     */
    DetachedCriteria getExistsCriteria(Map criteriaMap, Class linkedEntityClazz, String linkedIdJoinProperty){
        DetachedCriteria existsCrit
        if(criteriaMap.tags){
            //convert to id long list
            List<Long> tagIds = Transform.objectToLongList((List)criteriaMap.remove('tags'), 'id')
            existsCrit = buildExistsCriteria(tagIds, linkedEntityClazz, linkedIdJoinProperty)
        } else if(criteriaMap.tagIds) {
            //should be list of id if this key is present
            existsCrit = buildExistsCriteria((List)criteriaMap.remove('tagIds'),  linkedEntityClazz, linkedIdJoinProperty)
        }
        return existsCrit
    }

}
