/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.attachment.repo

import groovy.transform.CompileStatic

import gorm.tools.model.Persistable
import gorm.tools.repository.GormRepository
import gorm.tools.repository.model.AbstractLinkedEntityRepo
import gorm.tools.support.Results
import yakworks.rally.attachment.model.Attachable
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink

@GormRepository
@CompileStatic
class AttachmentLinkRepo extends AbstractLinkedEntityRepo<AttachmentLink, Attachment> {

    AttachmentLinkRepo(){
        super(Attachment)
    }

    @Override
    List<String> getPropNames() { ['linkedId', 'attachment']}

    @Override
    Persistable lookup(String type, Object data){
        //FIXME make a generic way to lookup id and code, for now only loads by id
        Attachment.load(data['id'] as Long)
    }

    @Override
    List<AttachmentLink> addOrRemove(Persistable entity, Object itemParams){
        def list = super.addOrRemove(entity, itemParams)
        updateAttachableHasAttachments(entity, list)
        return list
    }

    /**
     * updates the cached hasAttachments on the attachable entity
     */
    void updateAttachableHasAttachments(Persistable entity, List linkList){
        // update the has attachments
        if(Attachable.isAssignableFrom(entity.class)){
            def attachableEntity = (Attachable)entity
            attachableEntity.hasAttachments = linkList?.size()
        }
    }

    boolean hasAttachments(Persistable entity) {
        count(entity)
    }

    List<Attachment> listAttachments(Persistable entity) {
        listRelated(entity)
    }

    //
    // @RepoListener
    // void afterPersist(AttachmentLink activity, AfterPersistEvent e) {
    //     //FIXME this is a hack so the events for links get fired after data is inserted
    //     // not very efficient as removes batch inserting for lots of acts so need to rethink this strategy
    //     flush()
    // }

    /**
     * Copies Attachments from the source to target
     *
     * @param fromEntity entity to copy attachments from
     * @param toEntity entity to copy attachments to
     * @return the Results which will be ok or have errors if problem occured with IO
     */
    //XXX needs good test
    Results copy(Persistable fromEntity, Persistable toEntity) {
        Results results = Results.OK
        List attachLinks = queryFor(fromEntity).list()
        for(AttachmentLink attachLink : attachLinks){
            //catch exceptions and move on in case attachment has a bad link we dont want to fail the whole thing
            try {
                Attachment attachmentCopy = Attachment.repo.copy(attachLink.attachment)
                if (attachmentCopy) create(toEntity, attachmentCopy)
            } catch (ex){
                results.addError(ex)
            }
        }
        return results
    }

}
