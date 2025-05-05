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
import yakworks.commons.beans.Transform
import yakworks.commons.map.Maps
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
    void doExistsCriteria(Map criteriaMap, Class linkedEntityClazz, String linkedIdJoinProperty){
        Map mapWithTags = criteriaMap
        //convert to id long list, this assumes its in this format - {"tags": [{"id":1}, ..]}
        if (criteriaMap.containsKey('$not')) {
            Object notData = criteriaMap['$not']
            if (notData instanceof List){
                (notData as List<Map>).each {
                    if (it.containsKey('tags')) mapWithTags = it
                }
            } else if(notData instanceof Map){
                if (notData['tags']) mapWithTags = (Map)notData
            }
        }
        //if it has nothing then exit
        if(!mapWithTags.containsKey('tags')) return

        List tagIds = Maps.value(mapWithTags, 'tags.id.$in') as List
        tagIds = Transform.toLongList(tagIds) //make it long list
        mapWithTags.remove('tags') //remove it so its not picked up

        if(tagIds){
            mapWithTags['$exists'] = buildExistsCriteria(tagIds, linkedEntityClazz, linkedIdJoinProperty)
        }
    }

}
