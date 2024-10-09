/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.attachment.repo

import java.nio.file.Files
import java.nio.file.Path

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.PathResource
import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile

import gorm.tools.databinding.BindAction
import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.mango.api.QueryArgs
import gorm.tools.model.Persistable
import gorm.tools.repository.GormRepository
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.events.AfterBindEvent
import gorm.tools.repository.events.BeforeBindEvent
import gorm.tools.repository.events.BeforeRemoveEvent
import gorm.tools.repository.events.RepoListener
import gorm.tools.repository.model.LongIdGormRepo
import gorm.tools.validation.Rejector
import yakworks.commons.io.PathTools
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.tag.model.TagLink

/**
 * Attachments have some inherit complexity.  Please read this docs fully before amkign changes
 */
@Slf4j
@GormRepository
@CompileStatic
class AttachmentRepo extends LongIdGormRepo<Attachment> {
    public static final String ATTACHMENT_LOCATION_KEY = "attachments.location"

    @Autowired AttachmentSupport attachmentSupport
    @Autowired AttachmentLinkRepo attachmentLinkRepo

    /**
     * wraps super.bindAndCreate in try catch so that on any exception
     * it will delete the file reference in the data params
     */
    @Override
    void bindAndCreate(Attachment attachment, Map data, PersistArgs args) {
        try {
            //normal call to bindAndSave that occurs in super trait
            bindAndSave(attachment, data, BindAction.Create, args)
        } catch (e) {
            // the file may have been created in afterBind so delete it if exception fires
            Path attachedFile = data['attachedFile'] as Path
            if(attachedFile) Files.deleteIfExists(attachedFile)
            throw e
        }
    }

    /**
     * Called from doAfterPersist and before afterPersist event
     * if its had a bind action (create or update) and it has data
     * creates or updates One-to-Many associations for this entity.
     */
    @Override
    void doAfterPersistWithData(Attachment attachment, PersistArgs args) {
        Map data = args.data
        if(data.tags != null) TagLink.addOrRemoveTags(attachment, data.tags)
    }

    /**
     * The event listener, which is called before binding data to an Attachment entity
     * and before persisting this entity to a database.
     *
     * @param attachment an entity on which the bind operation was performed
     * @param p params data that will be bound to the entity
     * @param ev type of the action before which bind is performed, e.g. Create or Update
     */
    @RepoListener
    void beforeBind(Attachment attachment, Map p, BeforeBindEvent ev) {
        if (ev.isBindCreate()) {
            //id early so we have it for associations and file creation
            generateId(attachment)
            if (!p.name) p.name = p.originalFileName
            if (!p.name) {
                Rejector.of(attachment).withNotNullError('name')
                return
            }
            if (!p.mimeType) p.mimeType = PathTools.extractMimeType(p.name as String)
            if (!p.extension) p.extension = PathTools.getExtension(p.name as String)
            //FIXME hard coded design needs to be refactored out and simplified
            if (p.isCreditFile) p.locationKey = "attachments.creditFiles.location"
        }

        //DEPRECATED logic for storing data in db
        if (ev.isBindUpdate()) {
            if (p['fileData.data'] && p['fileData.data'] instanceof String) {
                String fdata = (p['fileData.data'] as String)
                p.contentLength = fdata.getBytes().size()
                p['fileData.data'] = fdata.getBytes()
            }
        }
    }

    @RepoListener
    void afterBind(Attachment attachment, Map p, AfterBindEvent ev) {
        if (ev.isBindCreate()) {
            if(attachment.hasErrors()) return //if it has errors the exit
            Path attachedFile = createFile(attachment, p)
            if(attachedFile){
                p.attachedFile = attachedFile //used later in exeption handling to delete the file
                attachment.location = attachmentSupport.getRelativePath(attachedFile, attachment.locationKey)
                attachment.contentLength = Files.size(attachedFile)
            } else if(attachment.fileData?.data) {
                attachment.contentLength = attachment.fileData.data.size()
            }
            // assert Files.exists(attachedFile)
            // assert attachment.locationKey
        }
    }

    // FIXME needs test
    @RepoListener
    void beforeRemove(Attachment attachment, BeforeRemoveEvent e) {
        if (attachment.location) {
            //delete the file
            attachmentSupport.deleteFile(attachment.location, attachment.locationKey)
        }
        //remove the links
        attachmentLinkRepo.remove(attachment)
        //tags
        TagLink.remove(attachment)
    }

    /**
     * Override query for custom search for Tags etc..
     */
    @Override
    MangoDetachedCriteria<Attachment> query(QueryArgs queryArgs, @DelegatesTo(MangoDetachedCriteria)Closure closure) {
        Map criteriaMap = queryArgs.qCriteria
        //NOTE: tags are handled in the TagsMangoCriteriaEventListener
        return getQueryService().query(queryArgs, closure)
    }

    /**
     * removes the link for the entity and removes the attachment
     */
    void removeAttachment(Persistable entity, Attachment attachment){
        attachmentLinkRepo.remove(entity, attachment)
        attachment.remove()
    }

    /**
     * 4 ways a file can be set via params
     *   1. with tempFileName key, where its a name of a file that has been uploaded
     *      to the tempDir location key
     *   2. with sourcePath, this should be a absolute path object or string
     *   3. with MultipartFile
     *   4. with bytes, similiar to MultiPartFile. if this is the case then name should have the info for the file
     * @return the path object for the file to link in location
     */
    Path createFile(Attachment attachment, Map p){
        String fileName = p.name as String
        // Validate.notNull(fileName)
        if (p.tempFileName) { //this would be primary way to upload files via UI and api
            return attachmentSupport.createFileFromTempFile(attachment.id, fileName, p.tempFileName as String, attachment.locationKey)
        }
        else if (p.sourcePath) { //used for copying attachments and testing
            return attachmentSupport.createFileFromSource(attachment.id, fileName, p.sourcePath as Path, attachment.locationKey)
        }
        else if (p.multipartFile) { //multipartFile from a ui
            MultipartFile multipartFile = p.multipartFile as MultipartFile
            Path tempFile = attachmentSupport.createTempFile(fileName, null)
            multipartFile.transferTo(tempFile) //do this instead of bytes as it is more memory efficient for big files
            return attachmentSupport.createFileFromTempFile(attachment.id, fileName, tempFile.fileName.toString(), attachment.locationKey)
        }
        else if (p.bytes && p.bytes instanceof byte[]) { //used for testing and string based templates
            return attachmentSupport.createFileFromBytes(attachment.id, fileName, p.bytes as byte[], attachment.locationKey)
        }
    }


    /**
     * creates from a multipart file as an attachment. Used in LogoService for example.
     *
     * @param multipartFile the MultipartFile that has the bytes and info
     * @param params any extra params for the Activity
     */
    Attachment create(MultipartFile multipartFile, Map params) {
        params['name'] = multipartFile.originalFilename
        //params['mimeType'] = multipartFile.contentType
        params['multipartFile'] = multipartFile

        create(params)
    }

    /**
     * Create from path
     */
    Attachment create(Path sourcePath, String name, Map data = [:]) {
        data['sourcePath'] = sourcePath
        data['name'] = name
        create(data)
    }


    Resource getResource(Attachment attachment){
        //attachmentSupport.getResource(attachment)
        Path path = getFile(attachment)
        return new PathResource(path)
    }

    Path getFile(Attachment attachment){
        attachmentSupport.getPath(attachment.location, attachment.locationKey)
    }

    String getDownloadUrl(Attachment attachment) {
        attachmentSupport.getDownloadUrl(attachment)
    }

    /**
     * Create a copy of the given attachment
     *
     * @param source  attachment which should be copied
     * @return a new attachment, which is copied from the source, in case 'source' is null - returns null
     */
    // @Transactional create is already in a trx and is enough
    Attachment copy(Attachment source) {
        if(source == null) return null
        Map params = [:]
        ['name', 'description', 'mimeType', 'kind', 'subject', 'locationKey'].each {String prop ->
            params[prop] = source[prop]
        }
        if(source.location) {
            params.sourcePath = getFile(source)
        }
        else if(source.fileData?.data) {
            params.fileData = [data: source.fileData.data] as Map
        }
        Attachment copy = create(params)
        return copy
    }

}
