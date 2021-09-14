package yakworks.rally.domain

import org.apache.commons.lang3.RandomStringUtils
import org.grails.web.mapping.DefaultLinkGenerator
import org.grails.web.mapping.UrlMappingsHolderFactoryBean

import gorm.tools.model.Persistable
import gorm.tools.security.testing.SecurityTest
import gorm.tools.testing.unit.DataRepoTest
import gorm.tools.testing.unit.DomainRepoTest
import grails.plugin.viewtools.AppResourceLoader
import spock.lang.IgnoreRest
import spock.lang.Specification
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityContact
import yakworks.rally.activity.model.ActivityLink
import yakworks.rally.activity.model.ActivityNote
import yakworks.rally.activity.repo.ActivityRepo
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgTag
import yakworks.rally.orgs.model.OrgTypeSetup
import yakworks.rally.tag.model.Tag
import yakworks.rally.tag.model.TagLink
import yakworks.rally.testing.MockHelper

import static yakworks.rally.activity.model.Activity.Kind as ActKinds

class ActivitySpec extends Specification implements DataRepoTest, SecurityTest { //implements SecuritySpecUnitTestHelper{
    //Sanity checks and auto runs DomainRepoCrudSpec tests

    void setupSpec(){
        defineBeans{
            appResourceLoader(AppResourceLoader) {
                grailsApplication = grailsApplication
            }
            attachmentSupport(AttachmentSupport)
        }
        mockDomains(AttachmentLink, ActivityLink, Activity, Org, OrgTag,
            Tag, TagLink, Attachment, ActivityNote, Contact, ActivityContact)
    }

    ActivityRepo activityRepo

    static Map getNoteParams(){
        return [
            org:[id:205],
            title: 'Todays test note',
            note:[body: 'Todays test note'],
            summary:'2+3=5'
        ]
    }

    List createSomeContacts(){
        Contact contact1 = MockHelper.contact([firstName: "bill"])
        Contact contact2 = MockHelper.contact([firstName: "bob"])
        [contact1, contact2]
    }


    void "creates note if summary is longer than 255"() {
        when:
        def params = [kind:"Note", org:MockHelper.org()]
        String summary = RandomStringUtils.randomAlphabetic(300)
        params.summary = summary
        Activity activity = Activity.create(params)

        then:
        activity.note != null
        activity.summary.length() == 255
        activity.note.body.length() == 300
        activity.note.body == summary
    }

    void "update note if exists"() {
        when:
        Org org = build(Org)
        Activity activity = build(Activity, [org:org])
        activity.persist()
        def params = [kind:"Note", id:activity.id]
        flushAndClear()
        String summary = RandomStringUtils.randomAlphabetic(300)
        params.summary = summary
        Activity updatedActivity = Activity.update(params)

        then:
        updatedActivity.note != null
        updatedActivity.summary.length() == 255
        updatedActivity.note.body.length() == 300
        updatedActivity.note.body == summary
    }

    void "save works on gorm6.1.11 but fails on 6.1.12"(){

        expect:
        def params = [
                org:[id: 205], //org id does not exist
                note:[body: 'Todays test note'],
                summary: 'will get overriden'
        ]

        Activity act = Activity.create(params)
        flush()
    }

    /*
    * Testcase for saving note without task or attachment
    */
    void "save and remove a simple note"(){
        setup:
        Org org = MockHelper.org()

        expect:
        org.id != null

        when:
        def params = [
                org    : [id: org.id],
                note   : [body: 'test note'],
                summary: '!! should get overriden as it has a note !!'
        ]

        Activity act = Activity.create(params)
        act.persist(flush:true)

        then:
        ActKinds.Note == act.kind
        null == act.title
        'test note' == act.summary
        'test note' == act.note.body

        when:
        flush()
        Activity activity = Activity.get(act.id)

        then:
        activity
        activity.note
        params.note.body == activity.note.body
        params.note.body == activity.summary

        ActKinds.Note == activity.kind
        activity.task == null

        when: "Activity is removed, note also gets removed"
        activity.remove()

        then:
        Activity.get(act.id) == null
        ActivityNote.get(activity.noteId) == null
    }

    void "test save with associations"() {
        setup:
        Org org = MockHelper.org()
        Tag t1 = build(Tag, [name: "T1", entityName: "Activity"])
        Tag t2 = build(Tag, [name: "T2", entityName: "Activity"])
        List contacts = createSomeContacts()

        List<Map> cmap = [[id:contacts[0].id], [id:contacts[1].id]]


        expect:
        org.id != null
        t1.id != null
        t2.id != null

        when:
        def params = [
            org    : [id: org.id],
            note   : [body: 'test note'],
            summary: 'The summary',
            tags: [[id:t1.id], [id:t2.id]],
            contacts: cmap
        ]

        Activity act = Activity.create(params)
        act.persist(flush:true)

        then:
        act != null
        act.tags.size() == 2
        act.tags[0].name == "T1"
        act.contacts.size() == 2
    }

    void testAddActivityContact() {
        when:
        Org org = build(Org)
        Activity act = build(Activity, [org:org])
        act.repo.addNote(act, "test body")
        act.persist()

        then:
        act.note?.id != null
        def act2 = Activity.findById(act.id)
        act2.note != null
        "test body" == act2.note.body
        "plain" == act2.note.contentType
    }

    void testUpdateSummary_WithNoteKind_AndShortBody(){

        when:
        Org org = build(Org)
        Activity activity = build(Activity, [org:org, summary: 'test summary'])
        activity.repo.addNote(activity, "test body")
        activity.persist()

        then:
        activity.id != null
        //Check summary of saved Activity
        activity.note.body == activity.summary
    }

    void testUpdateSummary_WithLongBody(){
        when:
        Org org = build(Org)
        Activity activity = build(Activity, [org:org, summary: 'test summary'])
        //Adding noteBody with more than 255 chars
        String noteBody = "DENIAL 1:  Proof of shipment enclosed.  Order shipped FOB Origin.  Please repay the deduction and address carton shrotage issues with your designated carrier. tt Matt Johnson w/Albuquerque Factory, said not valid sales allowance. LVM to Robert remind. new past due invoices."
        assert noteBody.trim().length() > 255
        activity.repo.addNote(activity, noteBody)
        activity.persist()

        then:
        activity.id != null
        //Check summary of saved Activity
        activity.note.body.trim().substring(0,251)+" ..." == activity.summary
        255 == activity.summary.length()
    }

    void testUpdateSummary_With255CharsBody(){
        setup:
        Org org = build(Org)
        Activity activity = build(Activity, [org:org, summary: 'test summary'])
        //Adding noteBody with exact 255 chars
        String noteBody = "DENIAL 1:  Proof of shipment enclosed.  Order shipped FOB Origin.  Please repay the deduction and address carton shrotage issues with your designated carrier. tt Matt Johnson w/Albuquerque Factory, said not valid sales allowance. LVM to Robert remind. new"

        expect:
        255 == noteBody.trim().length()

        when:
        activity.repo.addNote(activity, noteBody)
        activity.persist()

        then:
        activity.id != null
        //Check summary of saved Activity
        activity.note.body.trim() == activity.summary
        255 == activity.summary.length()
    }

    def "test removeActivityAttachment"() {
        setup:
        Attachment attachment = build(Attachment) //new Attachment(TestDataJson.buildMap([:], Attachment))
        attachment.location = "foo/bar"
        attachment.persist()
        def activity = build(Activity) as Persistable
        AttachmentLink.create(activity, attachment)

        Long attId = attachment.id

        flushAndClear()
        activity = Activity.get(activity.id)

        expect:
        attachment.name == "name"
        attachment.id != null

        activity.attachments[0] == AttachmentLink.get(activity, attachment).attachment

        when:
        Attachment.repo.removeAttachment(activity, attachment)
        flushAndClear()

        then:
        def act = Activity.get(activity.id)
        //ActivityAttachment.findAllByActivity(activity) == [] //can not verify this in unit test,it does not use tables
        act.attachments.size() == 0 //ensure attachment is removed from here
        Attachment.get(attId) == null

    }
}
