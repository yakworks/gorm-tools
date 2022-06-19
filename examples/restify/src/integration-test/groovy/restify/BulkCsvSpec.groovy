package restify

import gorm.tools.repository.RepoUtil
import gorm.tools.rest.controller.RestRepoApiController
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import yakworks.commons.io.FileUtil
import yakworks.commons.io.ZipUtils
import yakworks.commons.util.BuildSupport
import yakworks.gorm.testing.http.RestIntTest
import yakworks.grails.resource.AppResourceLoader
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.repo.AttachmentRepo
import yakworks.rally.job.SyncJob
import yakworks.rally.orgs.model.Contact

@Rollback
@Integration
class BulkCsvSpec  extends RestIntTest {

    RestRepoApiController<Contact> controller
    AppResourceLoader appResourceLoader
    AttachmentRepo attachmentRepo

    void setup() {
        controllerName = 'ContactController'
    }

    void "test upload zip file for bulk process"() {
        setup: "Create zip"
        //Org.create(num:"bulk1", name:"bulk1", companyId: Company.DEFAULT_COMPANY_ID).persist()

        File contactCsv =  new File(BuildSupport.gradleRootProjectDir, "examples/resources/csv/contact.csv")
        assert contactCsv.exists()

        File zip = ZipUtils.zip("test.zip", appResourceLoader.rootPath.toFile(), contactCsv)

        expect:
        zip.exists()

        when: "create attachment"
        Map params = [name: "test.zip", sourcePath: zip.toPath()]
        Attachment attachment
        Attachment.withNewTransaction {
            attachment = Attachment.create(params)
        }
        RepoUtil.flush()

        then:
        noExceptionThrown()
        attachment != null
        attachment.id != null
        attachment.resource.getFile().exists()

        when:
        controller.params.attachmentId = attachment.id
        controller.params['promiseEnabled'] = false //disable promise for test
        controller.params['dataFilename'] = "contact.csv"
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
        attachmentRepo.removeById(syncJob.dataId as Long)
        Attachment.withNewTransaction {
            if(attachment) attachment.remove()
            if(body.id) SyncJob.removeById(body.id as Long) //syncjob is created in new transaction
        }
        if(zip.exists()) zip.delete()
    }
}
