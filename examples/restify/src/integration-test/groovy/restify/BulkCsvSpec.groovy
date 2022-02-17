package restify

import gorm.tools.async.AsyncService
import gorm.tools.repository.RepoUtil
import gorm.tools.rest.controller.RestRepoApiController
import grails.gorm.transactions.Rollback
import grails.plugin.viewtools.AppResourceLoader
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.commons.io.FileUtil
import yakworks.commons.util.BuildSupport
import yakworks.gorm.testing.http.RestIntegrationTest
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.job.SyncJob
import yakworks.rally.orgs.model.Company
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org

@Rollback
@Integration
class BulkCsvSpec  extends Specification implements RestIntegrationTest {

    RestRepoApiController<Contact> controller
    AppResourceLoader appResourceLoader
    AsyncService asyncService

    void setup() {
        controllerName = 'ContactController'
    }

    void "test upload zip file for bulk process"() {
        setup: "Create zip"
        //Org.create(num:"bulk1", name:"bulk1", companyId: Company.DEFAULT_COMPANY_ID).persist()

        boolean asyncBck = asyncService.asyncEnabled
        asyncService.asyncEnabled = false //disable parallel for test
        File contactCsv =  new File(BuildSupport.gradleRootProjectDir, "examples/resources/csv/contact.csv")
        assert contactCsv.exists()

        File zip = FileUtil.zip("test.zip", appResourceLoader.rootLocation, contactCsv)

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
        controller.params['promiseEnabled'] = "" //disable promise for test
        controller.params['dataFilename'] = "contact.csv"

        controller.bulkCreate()
        Map body = response.bodyToMap()

        then:
        body != null
        body.state == "Finished"
        body.ok == true

        when: "Verify db records"
        List<Contact> created = Contact.findAllByNumLike("bulk_")

        then:
        3 == created.size()
        created[0].num == "bulk1"
        created[0].orgId == 1

        cleanup: "cleanup db"
        asyncService.asyncEnabled = asyncBck
        Attachment.withNewTransaction {
            if(attachment) attachment.remove()
            if(body.id) SyncJob.removeById(body.id as Long) //syncjob is created in new transaction
        }
        if(zip.exists()) zip.delete()
    }
}