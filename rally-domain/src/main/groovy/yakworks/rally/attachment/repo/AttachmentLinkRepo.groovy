/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.attachment.repo

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import gorm.tools.model.Persistable
import gorm.tools.repository.GormRepository
import gorm.tools.support.Results
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.common.LinkedEntityRepoTrait

@Slf4j
@GormRepository
@CompileStatic
class AttachmentLinkRepo implements LinkedEntityRepoTrait<AttachmentLink, Attachment> {
    AttachmentRepo attachmentRepo

    @Override
    String getItemPropName() {'attachment'}

    @Override
    Attachment loadItem(Long id) { Attachment.load(id)}

    /**
     * Copies Attachments from the source to target
     *
     * @param fromEntity entity to copy attachments from
     * @param toEntity entity to copy attachments to
     * @return the Results which will be ok or have errors if problem occured with IO
     */
    //XXX needs good test from 9ci rally
    Results copy(Persistable fromEntity, Persistable toEntity) {
        Results results = Results.OK
        List attachLinks = list(fromEntity)
        for(AttachmentLink attachLink : attachLinks){
            //catch exceptions and move on in case attachment has a bad link we dont want to fail the whole thing
            try {
                Attachment attachmentCopy = attachmentRepo.copy(attachLink.attachment)
                if (attachmentCopy) create(toEntity, attachmentCopy)
            } catch (ex){
                results.addError(ex)
            }
        }
        return results
    }
}
