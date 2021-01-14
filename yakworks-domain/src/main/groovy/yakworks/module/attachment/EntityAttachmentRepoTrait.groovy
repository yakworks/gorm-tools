/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.module.attachment

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.transaction.TransactionStatus

import gorm.tools.model.Persistable
import gorm.tools.repository.GormRepo
import yakworks.commons.lang.Validate
import yakworks.module.attachment.entity.Attachment

@Slf4j
@CompileStatic
trait EntityAttachmentRepoTrait<D> implements GormRepo<D> {

    D create(Persistable entity, Attachment attachment, Map args = [:]) {
        verifyEntityName(entity, attachment)
        create(entity.id, attachment, args)
    }

    D create(long linkedId, Attachment attachment, Map args = [:]) {
        D entityAttachment = (D) getEntityClass().newInstance(linkedId: linkedId, attachment: attachment)
        Map mArgs = [flush: false, insert: true]
        mArgs.putAll(args) //overrides
        gormInstanceApi().save entityAttachment, args
        entityAttachment
    }

    /**
     * override in implementation to throw IllegalArgumentException if the attachment.entityName does not match
     */
    void verifyEntityName(Persistable entity, Attachment attachment){
        // override in impl
        Validate.notNull(attachment)
    }

    D get(Persistable entity, Attachment attachment) {
        get(entity.id, attachment)
    }

    D get(long linkedId, Attachment attachment) {
        Serializable entityKey = (Serializable) getEntityClass().newInstance(linkedId: linkedId, attachment: attachment)
        gormStaticApi().get(entityKey)
    }

    @CompileDynamic //ok for now
    List<Attachment> listAttachments(long linkedId) {
        list(linkedId)*.attachment
    }

    List<D> list(long linkedId) {
        query(linkedId: linkedId).list()
    }

    boolean exists(long linkedId, long attachmentId) {
        query(linkedId: linkedId, attachment: Attachment.load(attachmentId)).count()
    }

    boolean exists(long linkedId, Attachment attachment) {
        query(linkedId: linkedId, attachment: attachment).count()
    }

    boolean remove(long linkedId, Attachment attachment) {
        if (linkedId && attachment != null) {
            query(linkedId: linkedId, attachment: attachment).deleteAll()
        }
    }

    Integer removeAll(long linkedId) {
        query(linkedId: linkedId).deleteAll() as Integer
    }

    void addAttachments(Persistable entity, List<Long> attachmentIdList) {
        gormStaticApi().withTransaction { TransactionStatus status ->
            for (Long attachmentId : attachmentIdList) {
                create(entity, Attachment.load(attachmentId), [:])
            }
        }
    }

    void removeAttachments(long linkedId, List<Long> attachmentIdList) {
        gormStaticApi().withTransaction { TransactionStatus status ->
            for (Long attachmentId : attachmentIdList) {
                remove(linkedId, Attachment.load(attachmentId))
            }
        }
    }

    void bindAttachments(Persistable entity, Object attachmentParams){
        if(!attachmentParams) return
        Long linkedId = entity.id

        //default is to replace the tags with whats in tagParams
        if (attachmentParams instanceof List) {
            def tagList = attachmentParams as List<Map>
            List<Long> tagParamIds = tagList.collect { it.id as Long }
            List<Long> currentTagIds = listAttachments(linkedId)*.id

            List<Long> tagsToAdd = tagParamIds - currentTagIds
            addAttachments(entity, tagsToAdd)

            List<Long> tagsToRemove = currentTagIds - tagParamIds
            removeAttachments(linkedId, tagsToRemove)

        } else if (attachmentParams instanceof Map &&  attachmentParams['op'] == 'remove') {
            removeAll(linkedId)
        }
    }

}
