/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.attachment.repo

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.springframework.core.io.Resource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.multipart.MultipartFile

import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.errors.EntityValidationException
import gorm.tools.repository.events.BeforeBindEvent
import gorm.tools.repository.events.BeforeRemoveEvent
import gorm.tools.repository.events.RepoListener
import gorm.tools.repository.model.IdGeneratorRepo
import grails.gorm.transactions.Transactional
import grails.plugin.viewtools.AppResourceLoader
import grails.web.mapping.LinkGenerator
import yakworks.commons.io.FileUtil
import yakworks.rally.attachment.model.Attachment

/**
 * Attachments are not as simple as they might be in this application.  Please read this documentation before messing
 * with it.
 *
 * There are three modes for attachments in this application:
 *   1.  fileData.data contains the file contents.  In this case, location is null when the attachment is saved.
 *   2.  location contains an absolute path starting with '/', which references the inside of the war file.
 *   3.  location contains a relative path, which places the file in the attachments directory.  This is described
 *       below.
 *
 * Attachment Directory Grand Strategy:
 * The production attachments directory is defined in Config.groovy or the external configuration file, defined by
 * nine.resources.attachments.location and will have a value looking something like this:
 * '${System.properties.getProperty('catalina.base')}/9ci-app/companies/${client.sourceId}-${client.id}/attachments'
 *
 * A test/demo Config.groovy (and likewise any external configuration on a Tomcat server) will contain:
 * nine.attachments.directory = "../database/resources/demo/companies/\${client.sourceId}-\${client.id}/attachments"
 *
 * This is the attachments directory for any given client.  The client cannot
 * escape from this directory through the application.  It's based on the sourceId of the client, plus the OID of the
 * client.  For example, it contains 'demo-2' for demo.
 *
 * Inside that, you get a directory named after the date and month that the attachment was created.  These directories
 * are automatically created when you insert an attachment into the Attachments directory.  Again, the users cannot
 * escape that once the attachment is created.  Here is an example path for an attachment for client 2:
 * ${catalina.base}/9ci-app/companies/demo-2/attachments/2010-11/214256.ftl
 * This would be for an attachment belonging to client 2 inserted on November of 2010.  The file name is the OID of
 * the attachment record plus the original extension of the file.
 *
 * For data which we want to be in Subversion for test or demo, we are handcrafting an Attachment row.  That means we can put the
 * actual files in 0000-00 and everyone will know that this is revisioned data.  Nothing else should be committed inside the
 * client's directory.  Here is an example file name for something we would check into Subversion:
 * ${catalina.base}/9ci-app/companies/demo-2/attachments/0000-00/214256.ftl
 *
 * Tests and demos can create new attachments.  If you are running in run-app or run-war, then those attachments will
 * go into this directory structure.  We DO NOT WANT to commit these files!  Please do not commit anything which is
 * not in a 0000-00 directory!
 * @author Ken Roberts
 */
@Slf4j
@GormRepository
@CompileStatic
class AttachmentRepo implements GormRepo<Attachment>, IdGeneratorRepo {

    AppResourceLoader appResourceLoader
    JdbcTemplate jdbcTemplate
    LinkGenerator grailsLinkGenerator
    AttachmentLinkRepo attachmentLinkRepo

    /**
     * The event listener, which is called before binding data to an Attachment entity
     * and before persisting this entity to a database.
     *
     * @param attachment an entity on which the bind operation was performed
     * @param p     data that will be bound to the entity
     * @param ev type of the action before which bind is performed, e.g. Create or Update
     */
    @RepoListener
    void beforeBind(Attachment attachment, Map p, BeforeBindEvent ev) {
        // p params loosely follows props in springs MultipartFile
        // originalFileName* - the original filename in the client's filesystem.
        // name* - the name of the attachment, will default to originalFileName if not sent
        //
        if (ev.isBindCreate()) {
            //**setup defaults
            //id early so we have it for parent child relationships
            if (!p['id']) p['id'] = generateId()
            if (!p['name']) p['name'] = p['originalFileName'] ?: p['subject']

            //if its null then try to get the proper extension, try name first, then originalFileName
            if (!p['extension']) {
                ['name', 'originalFileName', 'tempFileName'].each {
                    if (!p['extension']) p['extension'] = FilenameUtils.getExtension(p[it] as String)
                }
            }

            def data
            //if its has a tempFile property then its was already loaded and now needs to be grabbed and linked
            if (p['tempFileName']) {
                data = new File(appResourceLoader.getLocation('tempDir') as String, p['tempFileName'] as String)
                if (!data.exists()) throw new FileNotFoundException("Could not find temp file: ${p.tempFileName} , path: ${data.absolutePath}")
            } else if (p['bytes'] && p['bytes'] instanceof byte[]) {
                data = p['bytes']
            }

            String fname = FilenameUtils.getBaseName(p['name'] as String)

            String location = null

            //FIXME hard coded design needs to be refactored out
            if (p['isCreditFile']) location = "attachments.creditFiles.location"

            //Map createResult = appResourceLoader.createAttachmentFile(params.id, fname, params.extension, data, location)
            Map createResult = appResourceLoader.createAttachmentFile(p['id'] as Long, fname, p['extension'] as String, data, location as String)
            if (createResult) {
                File createdFile = createResult['file'] as File
                if (createdFile) {
                    p['file'] = createdFile
                    p['mimeType'] = FileUtil.extractMimeType(createdFile) ?: p['mimeType']
                }

                p['location'] = createResult['location']
                p['contentLength'] = createdFile?.size()

                if (log.debugEnabled) log.debug("Created an attachment at ${createdFile.absolutePath}")
            }
        }

        if (ev.isBindUpdate()) {
            if (p['fileData.data'] && p['fileData.data'] instanceof String) {
                String fdata = (p['fileData.data'] as String)
                p['contentLength'] = fdata.getBytes().size()
                p['fileData.data'] = fdata.getBytes()
            }
        }
    }

    /**
     * wraps super.doCreate in try catch so that on any exception it will delete the file reference in the data params
     */
    @Override
    Attachment doCreate(Map data, Map args) {

        Attachment attachment
        try {
            attachment = GormRepo.super.doCreate(data, args)
        } catch (e) {
            // the file may have been created in beforeBind so delete it if exception fires
            File file = data['file'] as File
            if (file?.exists()){
                file.delete()
            }
            throw e
        }

        attachment
    }

    /**
     * A listener, which is called before an Attachment entity removed.
     *
     * @param attachment the attachment which will be removed
     * @param params
     */
    @RepoListener
    void beforeRemove(Attachment attachment, BeforeRemoveEvent e) {
        File file = attachment.location ? appResourceLoader.getFile(attachment.location) : null
        if (file?.exists()) {
            file.delete()
        }
    }

    /**
     * Inserts the list of files into Attachments, and returns the attachments as a list
     * @param fileDetailsList a list of maps, see below
     * @return the list of attachments
     * <p>
     * fileDetailsList is a list of maps.  Each list entry (which is a map) represents a file.
     * The map has keys as follows:
     *    originalFileName: The name of the file the user sent.
     *    tempFileName: The name of the temp file the app server created to store it when uploaded.
     *    extension: The file extension
     */
    @Transactional
    List<Attachment> insertList(List<Map> fileDetailsList) {
        log.debug("*******-->File details list: ${fileDetailsList}")
        List<Attachment> resultList = []
        fileDetailsList.each { Map fileDetails ->
            Map fileParams = [
                name:fileDetails['originalFileName'],
                tempFileName:fileDetails['tempFileName'],
                extension: FilenameUtils.getExtension(fileDetails['originalFileName'] as String)
            ]
            Attachment attachment
            try{
                attachment = create(fileParams)
            }catch(e){
                resultList.each{
                    File file = new File(it.location)
                    if (file?.exists())
                        file.delete()
                }
                throw e
            }

            resultList.add(attachment)
        }

        resultList
    }

    /**
     * Saves a multipart file as an attachment. Used in LogoService for example.
     */
    Attachment insertMultipartFile(MultipartFile multipartFile, Map params) {
        params['name'] = params.name ?: multipartFile.originalFilename
        params['originalFileName'] = multipartFile.originalFilename
        params['mimeType'] = multipartFile.contentType
        params['bytes'] = multipartFile.bytes
        params['extension'] = FilenameUtils.getExtension(multipartFile.originalFilename)

        create(params)
    }

    Attachment insertMultipartFileCreditFile(MultipartFile multipartFile, Map params) {
        insertMultipartFile(multipartFile, params)
    }

    /**
     * Inserts an attachment with a FileData record as the data holder.  This is used exclusively for invoice templates.
     * expects a fileData.data param that has the data in it and makes that the data in the FileData to save
     * should also have size,extension and mimeType populated
     */
    Attachment insertToDbFileData(Map params) {
        if(params['fileData.data'] == null) {
            throw new EntityValidationException("Missing fileData.data")
        }

        params['contentLength'] = ((String)params['fileData.data']).getBytes().size()
        params['data'] = null
        create(params)
    }

    /**
     * Create a copy of the given attachment
     *
     * @param source  attachment which should be copied
     * @return a new attachment, which is copied from the source, in case 'source' is null - returns null
     */
    @Transactional
    Attachment copy(Attachment source) {
        if(source == null) return null
        InputStream inputStream = source.inputStream as InputStream
        if (!inputStream) throw new FileNotFoundException("Attachment ${source.location}")
        byte[] data = IOUtils.toByteArray(inputStream)
        if (data.length == 0) {
            throw new IOException("Attachment ${source.location} ByteArray is empty")
        }

        Map params = [name: source.name, bytes: data, extension: source.extension, description: source.description]
        Attachment copy = create(params)

        assert copy != null
        assert copy.id != null
        assert copy.id != source.id

        return copy
    }

    Resource getResource(Attachment attachment){
        File f = appResourceLoader.getFile(attachment.location)
        log.debug "File location is ${f.canonicalPath} which ${f.exists()?'exists.':'does not exist.'}"
        appResourceLoader.getResource("file:${f.canonicalPath}")
    }

    String getDownloadUrl(Attachment attachment) {
        grailsLinkGenerator.link(uri: "/attachment/download/${attachment.id}")
    }

}
