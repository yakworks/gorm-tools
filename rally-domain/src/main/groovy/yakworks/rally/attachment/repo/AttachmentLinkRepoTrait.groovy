/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.attachment.repo

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.transaction.TransactionStatus

import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.model.Persistable
import gorm.tools.repository.GormRepo
import yakworks.rally.attachment.model.Attachment

@Slf4j
@CompileStatic
trait AttachmentLinkRepoTrait<D> implements GormRepo<D> {

    Map getKeyMap(long linkedId, String linkedEntity, Attachment attachment){
        [linkedId: linkedId, linkedEntity: linkedEntity, attachment: attachment]
    }

    D create(Persistable entity, Attachment attachment, Map args = [:]) {
        create(entity.id, entity.class.simpleName, attachment, args)
    }

    D create(long linkedId, String linkedEntity, Attachment attachment, Map args = [:]) {
        def params = getKeyMap(linkedId, linkedEntity, attachment)
        D entityTag = (D) getEntityClass().newInstance(params)
        Map mArgs = [flush: false, insert: true, failOnError: true]
        mArgs.putAll(args) //overrides
        gormInstanceApi().save entityTag, args
        entityTag
    }

    String getEntityName(Persistable entity){
        entity.class.simpleName
    }

    D get(Persistable entity, Attachment attachment) {
        queryFor(entity, attachment).get()
    }

    D get(long linkedId, String linkedEntity, Attachment attachment) {
        def keyMap = getKeyMap(linkedId, linkedEntity, attachment)
        Serializable entityKey = (Serializable) getEntityClass().newInstance(keyMap)
        gormStaticApi().get(entityKey)
    }

    MangoDetachedCriteria<D> queryFor(Persistable entity){
        query(linkedId: entity.id, linkedEntity: entity.class.simpleName)
    }
    MangoDetachedCriteria<D> queryFor(Persistable entity, Attachment attachment){
        queryFor(entity).eq('attachment', attachment)
    }

    @CompileDynamic //ok for now
    List<Attachment> listAttachments(Persistable entity) {
        list(entity)*.attachment
    }

    List<D> list(Persistable entity) {
        queryFor(entity).list()
    }

    boolean exists(Persistable entity, Attachment attachment) {
        queryFor(entity, attachment).count()
    }

    boolean remove(Persistable entity, Attachment attachment) {
        queryFor(entity, attachment).deleteAll()
    }

    Integer removeAll(Persistable entity) {
        queryFor(entity).deleteAll() as Integer
    }

    void addAttachments(Persistable entity, List<Long> idList) {
        gormStaticApi().withTransaction { TransactionStatus status ->
            for (Long aid : idList) {
                create(entity, Attachment.load(aid), [:])
            }
        }
    }

    void removeAttachments(Persistable entity, List<Long> idList) {
        gormStaticApi().withTransaction { TransactionStatus status ->
            for (Long aid : idList) {
                remove(entity, Attachment.load(aid))
            }
        }
    }

    void bindAttachments(Persistable entity, Object attachParams){
        if(!attachParams) return
        Long linkedId = entity.id

        if (attachParams instanceof List) {
            def attachList = attachParams as List<Map>
            List<Long> attachParamIds = attachList.collect { it.id as Long }
            List<Long> currentAttachIds = listAttachments(entity)*.id

            List<Long> attachToAdd = attachParamIds - currentAttachIds
            addAttachments(entity, attachToAdd)

            List<Long> attachToRemove = currentAttachIds - attachParamIds
            removeAttachments(entity, attachToRemove)

        } else if (attachParams instanceof Map &&  attachParams['op'] == 'remove') {
            removeAll(entity)
        }
    }

}
