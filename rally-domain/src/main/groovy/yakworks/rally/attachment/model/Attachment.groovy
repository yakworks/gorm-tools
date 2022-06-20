/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.attachment.model

import java.nio.charset.Charset

import groovy.transform.CompileDynamic

import org.apache.commons.io.IOUtils
import org.springframework.core.io.Resource
import org.springframework.util.FileCopyUtils

import gorm.tools.audit.AuditStamp
import gorm.tools.audit.AuditStampTrait
import gorm.tools.model.NameDescription
import gorm.tools.repository.RepoLookup
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode
import yakworks.rally.attachment.repo.AttachmentRepo
import yakworks.rally.tag.model.Taggable

/**
 * Attachments refer to an externally created file, which could be a letter template, an image, text document or anything else.
 * We store attachments differently based on what type of attachment it is.
 * There are 3 types as yet:
 *   1.  Contained in the database in the FileData table.  This has a populated fileDataId and no location.  Invoice templates.
 *   2.  Contained in the war in web-app.  This has an unpopulated fileDataId and a location populated with the
 *       absolute path to the file.  For example, '/templates/invoice/greenbar.ftl' starting with a '/'
 *   3.  Contained in the Attachments directory.  This has an unpopulated fileDataId and a location populated with
 *       the relative path to the file.  For example, '2010-11-7/23452.ftl'.  In this case the location column
 *       contains the path relative to the Attachments directory which is defined in
 *       grailsApplication.config.nine.resources.attachments.location.
 * see AppResourceService for details.
 *
 * TODO Attachment may not be the best name, rename this to FileResource, **AppResource**, ResourceEntity)
 *  and have it implement the standard Spring Resource interface
 * @author Ken Roberts
 */
@IdEqualsHashCode
@AuditStamp
@Entity
@GrailsCompileStatic
class Attachment implements NameDescription, Taggable, AuditStampTrait, RepoEntity<Attachment>, Serializable {
    static final String DEFAULT_LOCATION_KEY = "attachments.location"

    //non persistable
    //TODO remove locationKey from transients when db is updated
    static transients = ["text", "resource", "linkGenerator", "downloadUrl", "createdByUser", 'locationKey']

    //cached copy of resource
    transient Resource resource //need transient modifier, so cache doesnt try to serialize it, see #rcm#4499

    //this should be the file display name without dir; foo.txt, bar.pdf, etc.
    // location has the relative path and unique name. Use description for any other display info
    String name // in the NameDescription trait here for constraints
    String description // in the NameDescription trait

    //the relative path to the locationKey, this is the name of the file. ex: 2012-02/somepdf.pdf or views/reports/arReport.ftl
    String location
    // the appResource config key to get the base directory that location is relative to
    // can also be used for future keys that designate storaage like S3 or linodes block
    String locationKey = DEFAULT_LOCATION_KEY
    //the file size/contentLength in bytes
    Long contentLength
    //the extension the file should have, can be
    String extension
    //The mime type of the file. most often this will be what you want the browser to see. Use "text/ftl" for freemarker
    String mimeType
    //the file data record if this bytes are stored in the database instead of a file location
    FileData fileData // MOSTLY DISCOURAGED!!! used for templates.
    //the kind of attachment. used to discern a template, used in reports too.
    Kind kind
    //optional value for a email template or collectionStep this is the generally the subject of an email or fax cover page.
    String subject

    String source = "9ci" //FIXME JD Ken - what is this for and why do we need it here?

    @CompileDynamic //Angry monkey? as of 4.0 GrailsCompileStatic bug needs this and it needs to come before constraints
    static enum Kind {
        Activity, Collection, Invoice, Report
    }

    static constraintsMap = [
        name:[ description: '''\
                This should be the file display name without dir; foo.txt, bar.pdf, etc.
                Populated from originalFileName when using a multiPart upload.
                Location has the relative path and unique name on system. Use description for any other useful info''',
            maxSize: 100 ],
        location:[ description: 'The relative path to the locationKey',
                 nullable: true, editable: false, display: false ],

        locationKey:[ description: 'Defaults to attachments.location but can be changed to another key such as creditFiles.location',
                 example: 'attachments.location', nullable: false, required: false],

        contentLength:[ description: 'The file size/contentLength in bytes. Populated on save',
                 example: 7896, nullable: true, editable: false],

        extension:[ description: 'The extension the file should have. Pulled from the name if not set. Helps dictate the mime-type',
                 example: 'pdf', nullable: true],

        mimeType:[ description: 'The mime type of the file. Will be pulled from the names extension',
                 example: 'application/pdf', nullable: true, required: false],

        fileData:[ display: false],

        subject:[ description: 'Optional value for a email template or collectionStep this is the generally the subject of an email or fax cover page.',
                 example: 'Customer', nullable: true],

        kind:[ description: 'The kind of attachment',
                 example: 'Activity', nullable: true],

        source:[ description: 'A source description if this is synced from another system',
                 nullable: true, maxSize: 50],
    ]

    static AttachmentRepo getRepo() { RepoLookup.findRepo(this) as AttachmentRepo }

    void setLocation(String val) {
        resource = null
        location = val
    }

    /*--Transients--*/
    /** if the file/data is text then this returns the String/Text */
    String getText() {
        if (getResource()?.exists()) {
            return FileCopyUtils.copyToString(new InputStreamReader(getResource().inputStream, Charset.defaultCharset()))
            // return IOUtils.toString(getResource().inputStream, Charset.defaultCharset())
        } else if (fileData?.data) {
            return new String(fileData.data, 'UTF-8')
        } else {
            return ''
        }
    }

    /** if the file/data is text then this returns the String/Text */
    InputStream getInputStream() {
        if (getResource()?.exists()) {
            return resource.inputStream
        } else if (fileData?.data != null) {
            return new ByteArrayInputStream(fileData.data)
        } else {
            return null
        }
    }

    Resource getResource() {
        //log.debug "location is ${location}"
        if (!resource && location) {
            resource = getRepo().getResource(this)
        }
        return resource
    }

    String getDownloadUrl() {
        getRepo().getDownloadUrl(this)
    }

    static mapping = {
        fileData column: 'fileDataId'
    }


}
