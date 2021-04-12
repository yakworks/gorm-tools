/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.attachment

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile

import grails.plugin.viewtools.AppResourceLoader
import yakworks.rally.attachment.model.Attachment

/**
 * Support for working with Attachment files
 */
@Slf4j
@CompileStatic
class AttachmentSupport {
    public static final String ATTACHMENTS_LOCATION_KEY = "attachments.location"

    AppResourceLoader appResourceLoader

    /**
     * takes a temp file name that will be in the tempDir locationKey as the
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
        return Files.copy(tempFile, attachmentFile)
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
     * @param sourceFile The absolute path of the file or temp file to be copied and linked.
     * @param locationKey defaults to 'attachments.location' but can be another config key to pass to appResourceLoader
     * @return the Path for the created attachment file.
     */
    Path createFile(Long id, Path sourceFile, String locationKey = ATTACHMENTS_LOCATION_KEY) {
        Path attachmentFile = getAttachmentsPath(id, sourceFile.getFileName().toString(), locationKey)
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
    Path getAttachmentsPath(String locationKey) {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        Path rootPath = appResourceLoader.getLocation(locationKey).toPath()
        Path attachmentPath = rootPath.resolve(datePart)
        //make sure it exists
        if(!Files.exists(attachmentPath)) Files.createDirectories(attachmentPath)
        return attachmentPath
    }

    /**
     * returns the relative path of the file to the dir for the config path locationKey.
     * so if locationKey='attachments' and appResourceLoader.getLocation returns '/var/9ci/attachments'
     * and the file is '/var/9ci/attachments/2020-12/foo123.jpg' this will return '2020-12/foo123.jpg'
     */
    String getRelativePath(String locationKey, Path file) {
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
     * Creates (or move) file relative to the MonthDirectory.
     * If the data param is a File then its the temp file, rename and move.
     * If data is a byte[] or a String then write to the new file
     *
     * @param attachmentId the ID of the Attachment record this file is going into.
     * @param name optional name of the file
     * @param extension The file extension to use.
     * @param locationKey The app.resources config key that points to the dir to store file,
     *                    should default to attachments.location
     * @param data The contents of the file as a File(temp file), String, byte array or null.
     *             If its null then returns null
     * @return A map
     *        location:(string of the location relative to rootLocation),
     *        file: the File instace that we put in that directory
     */
    Path createAttachmentFile(Long attachmentId, String name, String extension, String locationKey, Object data) {
        if (!data) return null

        String prefix = ""
        if (name) {
            prefix = "${name}_"
        } else if (data instanceof File) {
            //TODO we should have an option pass in a name so we can name it logically like we are doing with file
            prefix = "${data.name}_"
        }
        String destFileName = extension ? "${prefix}${attachmentId}.${extension}" : "${prefix}${attachmentId}"

        //setup the monthly dir for attachments
        File monthDir = appResourceLoader.getMonthDirectory(locationKey)
        File file = new File(monthDir, destFileName)

        Path p = Paths.get("/$destFileName");
        Files.createFile(p)

        if (data) {
            if (data instanceof File) FileUtils.moveFile(data, file)
            if (data instanceof byte[]) FileUtils.writeByteArrayToFile(file, data)
            if (data instanceof String) FileUtils.writeStringToFile(file, data)
        }
        //
        // String relPath = appResourceLoader.getRelativePath(locationKey, file)
        // return [location: relPath, file: file]
        return null
    }

    /**
     * updates params with the MultipartFile data
     */
    Map mergeMultipartFileParams(MultipartFile multipartFile, Map params) {
        params['name'] = params.name ?: multipartFile.originalFilename
        params['originalFileName'] = multipartFile.originalFilename
        params['mimeType'] = multipartFile.contentType
        params['bytes'] = multipartFile.bytes
        params['extension'] = FilenameUtils.getExtension(multipartFile.originalFilename)

        return params
    }


}
