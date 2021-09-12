/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.attachment.repo

import groovy.transform.CompileStatic

import gorm.tools.model.Persistable
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.AfterPersistEvent
import gorm.tools.repository.events.RepoListener
import gorm.tools.support.Results
import yakworks.commons.lang.Validate
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.common.LinkXRefRepo

@GormRepository
@CompileStatic
class AttachmentLinkRepo implements LinkXRefRepo<AttachmentLink, Attachment> {

    @Override
    String getItemPropName() {'attachment'}

    @Override
    Attachment loadItem(Long id) { Attachment.load(id)}

    @Override
    void validateCreate(Persistable linkEntity, Attachment attachment){
        Validate.notNull(linkEntity.id, "[linkEntity.id]")
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

    // @Override
    // List<AttachmentLink> replaceList(Persistable linkedEntity, List dataList){
    //     def itemList = dataList as List<Map>
    //     List<Long> itemParamIds = collectIds(itemList)
    //     List<Long> currentItemIds = listItemIds(linkedEntity)
    //
    //     List<Long> itemsToAdd = itemParamIds - currentItemIds
    //     List xlist = add(linkedEntity, itemsToAdd)
    //
    //     List<Long> itemsToRemove = currentItemIds - itemParamIds
    //     remove(linkedEntity, itemsToRemove)
    //     return xlist
    // }
}
