/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.attachment.model

import groovy.transform.CompileDynamic

import org.springframework.core.io.Resource

import gorm.tools.audit.AuditStamp
import gorm.tools.audit.AuditStampTrait
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.io.FileUtil
import yakworks.commons.transform.IdEqualsHashCode
import yakworks.rally.attachment.repo.AttachmentRepo
import yakworks.rally.common.NameDescription

/** Attachments refer to an externally created file, which could be a letter template, an image, text document or anything else.
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
 * @author Ken Roberts
 */

//TODO Attachment may not be the best name, rename this to
// FileDataResource(or.. FileResource, DocumentResource, FileDocResource, **AppResource** ?)
//and have it implement the standard Spring Resource interface
@IdEqualsHashCode
@AuditStamp
@Entity
@GrailsCompileStatic
class Attachment implements NameDescription, AuditStampTrait, RepoEntity<Attachment>, Serializable {
    static final String DEFAULT_LOCATION_KEY = "attachments.location"

    //non persistable
    static transients = ["text", "resource", "linkGenerator", "downloadUrl", "createdByUser"]

    //cached copy of resource
    transient Resource resource //need transient modifier, so cache doesnt try to serialize it, see #rcm#4499

    //this is the name of the file or view. for a view, it may have the controller prefix such as 'reports/someReport.ftl
    // String name // in the NameDescription trait
    // String description // in the NameDescription trait

    //the relative path to the locationKey, this is the name of the file. ex: 2012-02/somepdf.pdf or views/reports/arReport.ftl
    String location
    //the appResource config key to get the base directory that location is relative to
    String locationKey = DEFAULT_LOCATION_KEY
    //the file size/contentLength in bytes
    Long contentLength
    //the extension the file should have, can be
    String extension
    //The mime type of the file. most often this will be what you want the browser to see. Use "text/ftl" for freemarker
    String mimeType
    //the file data record if this bytes are stored in the database instead of a file location
    FileData fileData // MOSTLY DISCOURAGED!!! TODO Deprecate?, use the Attachments directory when possible.
    //the kind of attachment. used to discern a template, used in reports too.
    Kind kind
    //optional value for a email template or collectionStep this is the generally the subject of an email or fax cover page.
    String subject

    String source = "9ci" //FIXME JD Ken - what is this for and why do we need it here?

    @CompileDynamic //Angry monkey? as of 4.0 GrailsCompileStatic bug needs this and it needs to come before constraints
    static enum Kind {
        Activity, Collection, Invoice, Report
    }

    static AttachmentRepo getRepo() { RepoUtil.findRepo(this) as AttachmentRepo }

    void setLocation(String val) {
        resource = null
        location = val
    }

    /*--Transients--*/
    /** if the file/data is text then this returns the String/Text */
    String getText() {
        if (getResource()?.exists()) {
            return FileUtil.readFileToString(getResource().file)
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
        Attachment.log.debug "location is ${location}"
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

    static constraints = {
        NameDescriptionConstraints(delegate)
        mimeType nullable: true
        fileData nullable: true
        location nullable: true
        subject nullable: true, maxSize: 255
        extension nullable: true
        contentLength nullable: true
        kind nullable: true
    }

}
