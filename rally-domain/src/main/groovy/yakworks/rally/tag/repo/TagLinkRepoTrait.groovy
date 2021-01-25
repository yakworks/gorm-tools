/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.tag.repo

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.transaction.TransactionStatus

import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.model.Persistable
import gorm.tools.repository.GormRepo
import yakworks.rally.tag.model.Tag

@Slf4j
@CompileStatic
trait TagLinkRepoTrait<D> implements GormRepo<D> {

    Map getKeyMap(long linkedId, String linkedEntity, Tag tag){
        [linkedId: linkedId, linkedEntity: linkedEntity, tag: tag]
    }

    D create(Persistable entity, Tag tag, Map args = [:]) {
        verifyEntityName(entity, tag)
        //checkCreditTag(entity.creditInfo, tag)
        create(entity.id, entity.class.simpleName, tag, args)
    }

    D create(long linkedId, String linkedEntity, Tag tag, Map args = [:]) {
        def params = getKeyMap(linkedId, linkedEntity, tag)
        D entityTag = (D) getEntityClass().newInstance(params)
        Map mArgs = [flush: false, insert: true, failOnError: true]
        mArgs.putAll(args) //overrides
        gormInstanceApi().save entityTag, args
        entityTag
    }

    /**
     * override in implementation to throw IllegalArgumentException if the tag.entityName does not match
     */
    void verifyEntityName(Persistable entity, Tag tag){
        def entName = getEntityName(entity)
        if (!tag.isValidFor(entName))
            throw new IllegalArgumentException("Tag [${tag.name}] not valid for $entName, restricted with entityName:${tag.entityName}")
    }

    String getEntityName(Persistable entity){
        entity.class.simpleName
    }

    D get(Persistable entity, Tag theTag) {
        queryFor(entity, theTag).get()
    }

    D get(long linkedId, String linkedEntity, Tag theTag) {
        def keyMap = getKeyMap(linkedId, linkedEntity, theTag)
        Serializable entityKey = (Serializable) getEntityClass().newInstance(keyMap)
        gormStaticApi().get(entityKey)
    }

    MangoDetachedCriteria<D> queryFor(Persistable entity){
        query(linkedId: entity.id, linkedEntity: entity.class.simpleName)
    }
    MangoDetachedCriteria<D> queryFor(Persistable entity, Tag tag){
        queryFor(entity).eq('tag', tag)
    }

    @CompileDynamic //ok for now
    List<Tag> listTags(Persistable entity) {
        list(entity)*.tag
    }

    List<D> list(Persistable entity) {
        queryFor(entity).list()
    }

    boolean exists(Persistable entity, Tag tag) {
        queryFor(entity, tag).count()
    }

    boolean remove(Persistable entity, Tag tag) {
        queryFor(entity, tag).deleteAll()
    }

    Integer removeAll(Persistable entity) {
        queryFor(entity).deleteAll() as Integer
    }

    void addTags(Persistable entity, List<Long> tagIdList) {
        gormStaticApi().withTransaction { TransactionStatus status ->
            for (Long tagId : tagIdList) {
                create(entity, Tag.load(tagId), [:])
            }
        }
    }

    void removeTags(Persistable entity, List<Long> tagIdList) {
        gormStaticApi().withTransaction { TransactionStatus status ->
            for (Long tagId : tagIdList) {
                remove(entity, Tag.load(tagId))
            }
        }
    }

    void bindTags(Persistable entity, Object tagParams){
        if(!tagParams) return
        Long linkedId = entity.id

        //default is to replace the tags with whats in tagParams
        if (tagParams instanceof List) {
            def tagList = tagParams as List<Map>
            List<Long> tagParamIds = tagList.collect { it.id as Long }
            List<Long> currentTagIds = listTags(entity)*.id

            List<Long> tagsToAdd = tagParamIds - currentTagIds
            addTags(entity, tagsToAdd)

            List<Long> tagsToRemove = currentTagIds - tagParamIds
            removeTags(entity, tagsToRemove)

        } else if (tagParams instanceof Map &&  tagParams['op'] == 'remove') {
            removeAll(entity)
        }
    }

}
