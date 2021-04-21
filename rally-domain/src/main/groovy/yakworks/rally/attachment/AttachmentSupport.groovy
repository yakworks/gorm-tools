/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.attachment

import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.apache.commons.io.FilenameUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

import grails.plugin.viewtools.AppResourceLoader
import grails.web.mapping.LinkGenerator
import yakworks.rally.attachment.model.Attachment

/**
 * Support for working with Attachment files, much of it calls out to AppResourceLoader and converts to nio2 Path objects
 */
@Service @Lazy
@Slf4j
@CompileStatic
class AttachmentSupport {
    public static final String ATTACHMENTS_LOCATION_KEY = "attachments.location"

    @Autowired AppResourceLoader appResourceLoader
    @Autowired LinkGenerator grailsLinkGenerator

    /**
     * Move a temp file. takes a temp file name that will be in the tempDir locationKey as the
     * source for the linked attachment file.
     *
     * @param attachmentId the ID of the Attachment record this file is going into.
     * @param originalFileName The file name to use and concat with attachmentId, tempFileName will be munged with unique UUID
     * @param tempFileName The name of the file in the tempDir.
     * @param locationKey defaults to 'attachments.location' but can be another config key to pass to appResourceLoader
     * @return the Path for the created attachment file.
     */
    Path createFileFromTempFile(Long id, String originalFileName, String tempFileName, String locationKey = ATTACHMENTS_LOCATION_KEY) {
        Path attachmentFile = getAttachmentsPath(id, originalFileName, locationKey)
        Path tempFile = getTempPath().resolve(tempFileName)
        if(!Files.exists(tempFile)) throw new FileNotFoundException("Could not find temp file: ${tempFile.toString()}")
        return Files.move(tempFile, attachmentFile)
    }

    /**
     * creates a file from bytes
     *
     * @param id the ID of the Attachment record this file is going into.
     * @param originalFileName The file name to use and concat with attachmentId
     * @param bytes the byte array to use
     * @param locationKey defaults to 'attachments.location' but can be another config key to pass to appResourceLoader
     * @return the Path for the created attachment file.
     */
    Path createFileFromBytes(Long id, String originalFileName, byte[] bytes, String locationKey = ATTACHMENTS_LOCATION_KEY) {
        Path attachmentFile = getAttachmentsPath(id, originalFileName, locationKey)
        return Files.write(attachmentFile, bytes)
    }

    /**
     * Copies source/temp file for a Attachment linked file to the attachment dir.
     *
     * @param attachmentId the ID of the Attachment record this file is going into.
     * @param originalFileName The file name to use and concat with attachmentId, can be null and will use name of sourceFile
     * @param sourceFile The absolute path of the file or temp file to be copied and linked.
     * @param locationKey defaults to 'attachments.location' but can be another config key to pass to appResourceLoader
     * @return the Path for the created attachment file.
     */
    Path createFileFromSource(Long id, String originalFileName, Path sourceFile, String locationKey = ATTACHMENTS_LOCATION_KEY) {
        if(!originalFileName) originalFileName = sourceFile.getFileName().toString()
        Path attachmentFile = getAttachmentsPath(id, originalFileName, locationKey)
        return Files.copy(sourceFile, attachmentFile)
    }

    /**
     * creates unique file name by concating id and
     * gets the directory for attachments using appResourceLoader.getLocation and appending the
     * date yyyy-MM sub-directory and the unique file name
     */
    Path getAttachmentsPath(Long id, String fileName, String locationKey = ATTACHMENTS_LOCATION_KEY) {
        String attachFileName = concatFileNameId(fileName, id)
        return getAttachmentsPath(locationKey).resolve(attachFileName)
    }

    /**
     * gets the directory for attachments using appResourceLoader.getLocation and appending the
     * date yyyy-MM sub-directory.
     */
    Path getAttachmentsPath(String locationKey = ATTACHMENTS_LOCATION_KEY) {
        Path rootPath = appResourceLoader.getLocation(locationKey).toPath()
        Path attachmentPath = rootPath.resolve(getMonthDir())
        //make sure it exists
        if(!Files.exists(attachmentPath)) Files.createDirectories(attachmentPath)
        return attachmentPath
    }

    /**
     * returns the relative path of the file to the dir for the config path locationKey.
     * so if locationKey='attachments' and appResourceLoader.getLocation returns '/var/9ci/attachments'
     * and the file is '/var/9ci/attachments/2020-12/foo123.jpg' this will return '2020-12/foo123.jpg'
     */
    String getRelativePath(Path file, String locationKey = ATTACHMENTS_LOCATION_KEY) {
        Path rootPath = appResourceLoader.getLocation(locationKey).toPath()
        Path relativePath = rootPath.relativize(file)
        return relativePath.toString()
    }

    /**
     * uses appResourceLoader to get the tempDir pat
     */
    Path getTempPath() {
        appResourceLoader.getTempDir().toPath()
    }

    /**
     * use appResourceLoader to create a temp file
     *
     * @param originalFileName the name of the file that was uploaded
     * @param data is the file contents, and can be String, byte[], or null.
     * @return a Path instance pointing to file
     */
    Path createTempFile(String originalFileName, Object data){
        appResourceLoader.createTempFile(originalFileName, data).toPath()
    }

    Path getFile(String location, String locationKey = ATTACHMENTS_LOCATION_KEY) {
        Path rootPath = appResourceLoader.getLocation(locationKey).toPath()
        Path attachmentPath = rootPath.resolve(location)
        return attachmentPath
    }

    /**
     * Deletes a file if it exists.
     *
     * @param location the path to the file to delete
     *
     * @return  {@code true} if the file was deleted by this method; {@code
     *          false} if the file could not be deleted because it did not
     *          exist
     */
    boolean deleteFile(String location, String locationKey = ATTACHMENTS_LOCATION_KEY) {
        Path attachedFile = getFile(location, locationKey)
        if(attachedFile) return Files.deleteIfExists(attachedFile)
    }


    Resource getResource(Attachment attachment){
        File f = appResourceLoader.getFile(attachment.location)
        log.debug "File location is ${f.canonicalPath} which ${f.exists()?'exists.':'does not exist.'}"
        appResourceLoader.getResource("file:${f.canonicalPath}")
    }

    String getDownloadUrl(Attachment attachment) {
        grailsLinkGenerator.link(uri: "/attachment/download/${attachment.id}")
    }

    /****** STATICS ********/

    /**
     * returns a yyyy-MM sub-directory from current date
     */
    static String getMonthDir(){
        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
    }

    /**
     *  builds a unique file name for the file by appending the id to the name.
     *  for example, with baseName: foo.txt , attachmentId: 123, would return foo_123.txt
     *  @return the unique file name
     */
    static String concatFileNameId(String fileName, Long id) {
        String extension = FilenameUtils.getExtension(fileName)
        if(extension) extension = ".${extension}"

        String baseName = FilenameUtils.getBaseName(fileName)
        return "${baseName}_${id}${extension}"
    }

    /**
     * updates params with the MultipartFile data
     */
    static Map mergeMultipartFileParams(MultipartFile multipartFile, Map params) {
        params['name'] = params.name ?: multipartFile.originalFilename
        params['originalFileName'] = multipartFile.originalFilename
        params['mimeType'] = multipartFile.contentType
        params['bytes'] = multipartFile.bytes
        params['extension'] = FilenameUtils.getExtension(multipartFile.originalFilename)

        return params
    }


}
