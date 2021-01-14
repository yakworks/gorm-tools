/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.module.tag

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.transaction.TransactionStatus

import gorm.tools.model.Persistable
import gorm.tools.repository.GormRepo
import yakworks.module.tag.entity.Tag

@Slf4j
@CompileStatic
trait EntityTagRepoTrait<D> implements GormRepo<D> {

    D create(Persistable entity, Tag tag, Map args = [:]) {
        verifyEntityName(entity, tag)
        //checkCreditTag(entity.creditInfo, tag)
        create(entity.id, tag, args)
    }

    D create(long linkedId, Tag tag, Map args = [:]) {
        D entityTag = (D) getEntityClass().newInstance(linkedId: linkedId, tag: tag)
        Map mArgs = [flush: false, insert: true]
        mArgs.putAll(args) //overrides
        gormInstanceApi().save entityTag, args
        entityTag
    }

    /**
     * override in implementation to throw IllegalArgumentException if the tag.entityName does not match
     */
    void verifyEntityName(Persistable entity, Tag tag){
        // override in impl
    }

    D get(Persistable entity, Tag theTag) {
        get(entity.id, theTag)
    }

    D get(long linkedId, Tag theTag) {
        Serializable entityKey = (Serializable) getEntityClass().newInstance(linkedId: linkedId, tag: theTag)
        gormStaticApi().get(entityKey)
    }

    @CompileDynamic //ok for now
    List<Tag> listTags(long linkedId) {
        list(linkedId)*.tag
    }

    List<D> list(long linkedId) {
        query(linkedId: linkedId).list()
    }

    boolean exists(long linkedId, long tagId) {
        query(linkedId: linkedId, tag: Tag.load(tagId)).count()
    }

    boolean exists(long linkedId, Tag theTag) {
        query(linkedId: linkedId, tag: theTag).count()
    }

    boolean remove(long linkedId, Tag theTag) {
        if (linkedId && theTag != null) {
            query(linkedId: linkedId, tag: theTag).deleteAll()
        }
    }

    Integer removeAll(long linkedId) {
        query(linkedId: linkedId).deleteAll() as Integer
    }

    void addTags(Persistable entity, List<Long> tagIdList) {
        gormStaticApi().withTransaction { TransactionStatus status ->
            for (Long tagId : tagIdList) {
                create(entity, Tag.load(tagId), [:])
            }
        }
    }

    void removeTags(long linkedId, List<Long> tagIdList) {
        gormStaticApi().withTransaction { TransactionStatus status ->
            for (Long tagId : tagIdList) {
                remove(linkedId, Tag.load(tagId))
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
            List<Long> currentTagIds = listTags(linkedId)*.id

            List<Long> tagsToAdd = tagParamIds - currentTagIds
            addTags(entity, tagsToAdd)

            List<Long> tagsToRemove = currentTagIds - tagParamIds
            removeTags(linkedId, tagsToRemove)

        } else if (tagParams instanceof Map &&  tagParams['op'] == 'remove') {
            removeAll(linkedId)
        }
    }

}
