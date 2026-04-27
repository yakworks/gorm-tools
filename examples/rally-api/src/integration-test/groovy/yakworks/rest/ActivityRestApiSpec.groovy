package yakworks.rest

import java.nio.file.Files
import java.nio.file.Path

import grails.testing.mixin.integration.Integration
import okhttp3.Response
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import spock.lang.Specification
import yakworks.commons.lang.IsoDateUtil
import yakworks.rally.activity.model.Activity
import yakworks.rally.attachment.AttachmentSupport

import yakworks.rally.orgs.model.Org
import yakworks.rest.client.OkHttpRestTrait

import java.time.LocalDateTime

@Integration
class ActivityRestApiSpec  extends Specification implements OkHttpRestTrait {

    @Autowired
    AttachmentSupport attachmentSupport

    def setup(){
        login()
    }

    void "sanity check create"() {
        when:
        LocalDateTime now = LocalDateTime.now()
        Response resp = post("/api/rally/activity", [org:[id: 2], name: "test-note", actDate:now])
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.CREATED.value()
        body.name == "test-note"
        body.org.id == 2
        body.kind == 'Note'
        body.actDate == IsoDateUtil.format(now)
    }

    void "actDate is not updateable"() {
        setup:
        Activity act
        Activity.withNewTransaction {
            act = Activity.create([org: Org.load(10), note: [body: 'Test note']])
        }

        expect:
        act
        act.id
        act.actDate

        when:
        Response resp = put("/api/rally/activity/$act.id", [id:act.id, actDate: LocalDateTime.now()])
        Map body = bodyToMap(resp)

        then:
        resp.code() == 422
        !body.ok
        body.errors
        body.errors.size() == 1
        body.errors[0].code == "error.notupdateable"
        body.errors[0].field == "actDate"

        cleanup:
        if(act) {
            Activity.withNewTransaction {
                Activity.repo.removeById(act.id)
            }
        }
    }

    //tests tht activity can be updated with list of attachments and doesnt fail see #3569
    void "create activity with attachment and update with attachments unchanged"() {
        given:
        Long activityId = null
        Path tempFilePath = null

        when: "create note with one attachment (temp file in upload temp dir, same as UI/API upload flow)"
        LocalDateTime now = LocalDateTime.now()
        String fileName = 'activity-attachment-test.txt'
        String initialNote = 'note with attachment for rest'
        Path tmp = attachmentSupport.createTempFile(fileName, 'test text'.getBytes())
        tempFilePath = tmp
        String tempFileName = tmp.fileName.toString()
        Map createBody = [
            org        : [id: 2],
            name       : initialNote,
            note       : [body: initialNote],
            actDate    : now,
            attachments: [[tempFileName: tempFileName, originalFileName: fileName]]
        ]
        Response createResp = post("/api/rally/activity", createBody)
        Map created = bodyToMap(createResp)

        then:
        createResp.code() == HttpStatus.CREATED.value()
        created.id
        created.note.body == initialNote

        when: "load activity from API and verify attachment"
        activityId = created.id as Long
        Response getResp = get("/api/rally/activity/$activityId")
        Map body = bodyToMap(getResp)

        then:
        getResp.code() == HttpStatus.OK.value()
        body
        body.id
        body.attachments
        (body.attachments as List).size() == 1

        when: "update note only; attachments unchanged"
        Map att = (body.attachments as List)[0] as Map
        Long attachmentId = att.id as Long
        Map updateBody = [
            id         : activityId,
            note       : [body: 'Updated via REST; attachment metadata only'],
            attachments: [[id: attachmentId, name: fileName]]
        ]
        Response putResp = put("/api/rally/activity/$activityId", updateBody)
        Map updated = bodyToMap(putResp)

        then:
        putResp.code() == HttpStatus.OK.value()
        updated.note.body == updateBody.note.body

        when:
        Response getAfter = get("/api/rally/activity/$activityId")
        Map after = bodyToMap(getAfter)
        Map attAfter = (after.attachments as List)[0] as Map

        then:
        getAfter.code() == HttpStatus.OK.value()
        (after.attachments as List).size() == 1
        attAfter.id == attachmentId
        attAfter.name == fileName

        cleanup:
        if (tempFilePath) {
            Files.deleteIfExists(tempFilePath)
        }
        if (activityId) {
            Activity.withNewTransaction {
                Activity.repo.removeById(activityId)
            }
        }
    }
}
