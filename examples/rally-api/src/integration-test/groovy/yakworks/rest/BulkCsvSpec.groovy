package yakworks.rest

import yakworks.testing.gorm.integration.SecuritySpecHelper

import java.nio.file.Path

import gorm.tools.transaction.TrxUtils
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import yakworks.commons.io.ZipUtils
import yakworks.commons.util.BuildSupport

import yakworks.rally.attachment.model.Attachment
import yakworks.rally.job.SyncJob
import yakworks.rally.orgs.model.Contact
import yakworks.rest.gorm.controller.CrudApiController
import yakworks.testing.rest.RestIntTest

@Rollback
@Integration
class BulkCsvSpec  extends RestIntTest implements SecuritySpecHelper {

    CrudApiController<Contact> controller

    void setup() {
        controllerName = 'ContactController'
    }

    void "test upload zip file for bulk process"() {
        setup: "Create zip"
        //Org.create(num:"bulk1", name:"bulk1", companyId: Company.DEFAULT_COMPANY_ID).persist()
        Path resDir = BuildSupport.rootProjectPath.resolve('examples/resources')
        Path contactCsv =  resDir.resolve("csv/contact.csv")
        assert contactCsv.exists()

        File zip = ZipUtils.zip("test.zip", resDir.toFile(), contactCsv.toFile())

        expect:
        zip.exists()

        when: "create attachment"
        Map params = [name: "test.zip", sourcePath: zip.toPath()]
        Attachment attachment
        Attachment.withNewTransaction {
            attachment = Attachment.create(params)
        }
        TrxUtils.flush()

        then:
        noExceptionThrown()
        attachment != null
        attachment.id != null
        attachment.resource.getFile().exists()

        when:
        controller.params.attachmentId = attachment.id
        controller.params['async'] = false //disable promise for test
        controller.params['payloadFilename'] = "contact.csv"
        controller.params['saveDataAsFile'] = true //write to file

        controller.bulkCreate()
        Map body = response.bodyToMap()

        then:
        body != null
        body.state == "Finished"
        body.ok == true
        body.id != null

        and: "sanity check response"
        body.data instanceof Collection
        body.data[0].ok == true
        body.data[0].data instanceof Map
        body.data[0].data.num == "bulk1"

        when: "Verify db records"
        List<Contact> created = Contact.findAllByNumLike("bulk_")

        then:
        3 == created.size()
        created[0].num == "bulk1"
        created[0].orgId == 1

        when: "Verify syncjob"
        SyncJob syncJob =  SyncJob.get(body.id as Long)

        then:
        syncJob  != null
        syncJob.dataId != null //should have been set for bulk csv.

        cleanup: "cleanup db"
        if(body.id) SyncJob.repo.removeById(body.id as Long)
        Attachment.withNewTransaction {
            Contact.findAllByNumLike("bulk_").each {
                it.remove()
            }
        }
        if(zip.exists()) zip.delete()
    }

    void "test upload single csv file for bulk process"() {

        when: "create attachment"
        def csvFile = BuildSupport.rootProjectPath.resolve("examples/resources/csv/contact.csv")
        Map params = [name: csvFile.fileName.toString(), sourcePath: csvFile]
        Attachment attachment
        Attachment.withNewTransaction {
            attachment = Attachment.create(params)
        }
        TrxUtils.flush()

        then: "make sure attachment is good"
        noExceptionThrown()
        attachment != null
        attachment.id != null
        attachment.resource.getFile().exists()

        when:
        controller.params.attachmentId = attachment.id
        controller.params['async'] = false //disable promise for test
        controller.params['saveDataAsFile'] = true //write to file

        controller.bulkCreate()
        Map body = response.bodyToMap()

        then:
        body != null
        body.state == "Finished"
        body.ok == true
        body.id != null

        and: "sanity check response"
        body.data instanceof Collection
        body.data[0].ok == true
        body.data[0].data instanceof Map
        body.data[0].data.num == "bulk1"

        when: "Verify db records"
        List<Contact> created = Contact.findAllByNumLike("bulk_")

        then:
        3 == created.size()
        created[0].num == "bulk1"
        created[0].orgId == 1

        when: "Verify syncjob"
        SyncJob syncJob =  SyncJob.get(body.id as Long)

        then:
        syncJob  != null
        syncJob.dataId != null //should have been set for bulk csv.

        cleanup: "cleanup db"
        if(body.id) SyncJob.repo.removeById(body.id as Long)
        Attachment.withNewTransaction {
            Contact.findAllByNumLike("bulk_").each {
                it.remove()
            }
        }
    }

    void "test bulk update with csv"() {

        expect:
        Contact.repo.lookup(num:"secondary11").lastName

        when: "create attachment"
        def csvFile = BuildSupport.rootProjectPath.resolve("examples/resources/csv/contact-update.csv")
        Map params = [name: csvFile.fileName.toString(), sourcePath: csvFile]
        Attachment attachment
        Attachment.withNewTransaction {
            attachment = Attachment.create(params)
        }
        TrxUtils.flush()

        then: "make sure attachment is good"
        noExceptionThrown()
        attachment != null
        attachment.id != null
        attachment.resource.getFile().exists()

        when:
        controller.params.attachmentId = attachment.id
        controller.params['async'] = false //disable promise for test

        controller.bulkUpdate()
        Map body = response.bodyToMap()
        flushAndClear()

        then:
        body != null
        body.state == "Finished"
        body.ok == true
        body.id != null

        when: "Verify db records"
        Contact c10 = Contact.repo.lookup(num:"secondary10")
        Contact c11 = Contact.repo.lookup(num:"secondary11")

        then:
        c10.lastName == null //should have been nulled out.... rally seed data has org with comments
        c11.lastName == "test" //should have been updated

        cleanup: "cleanup db"
        if(body.id) SyncJob.repo.removeById(body.id as Long)
    }

    void "test bad CSV"() {

        when: "create attachment"
        def csvFile = BuildSupport.rootProjectPath.resolve("examples/resources/csv/contact-bad.csv")
        Attachment attachment
        Attachment.withNewTransaction {
            attachment = Attachment.create([name: csvFile.fileName.toString(), sourcePath: csvFile])
        }
        TrxUtils.flush()
        controller.params.attachmentId = attachment.id
        controller.params['async'] = false //disable promise for test
        controller.params['saveDataAsFile'] = true //write to file

        controller.bulkCreate()
        Map body = response.bodyToMap()

        then:
        response.status == 400

        body.ok == false
        body.code == 'error.data.csv'
        body.title == "CSV Data Problem"
        body.detail.contains "Error on record number 2"

        cleanup: "cleanup db"
        Attachment.withNewTransaction {
            if(attachment) attachment.remove()
            //if(body.id) SyncJob.repo.removeById(body.id as Long) //syncjob is created in new transaction
        }
    }
}
