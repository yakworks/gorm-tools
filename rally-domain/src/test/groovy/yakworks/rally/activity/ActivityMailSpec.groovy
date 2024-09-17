package yakworks.rally.activity

import org.apache.commons.lang3.RandomStringUtils

import gorm.tools.model.Persistable
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityContact
import yakworks.rally.activity.model.ActivityLink
import yakworks.rally.activity.model.ActivityNote
import yakworks.rally.activity.model.TaskType
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.mail.model.MailMessage
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgTag
import yakworks.rally.tag.model.Tag
import yakworks.rally.tag.model.TagLink
import yakworks.rally.testing.MockData
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

import static yakworks.rally.activity.model.Activity.Kind as ActKinds

class ActivityMailSpec extends Specification implements GormHibernateTest, SecurityTest {
    static entityClasses = [
        AttachmentLink, ActivityLink, MailMessage, Activity, TaskType, Org, OrgTag,
        Tag, TagLink, Attachment, ActivityNote, Contact, ActivityContact
    ]
    static springBeans = [AttachmentSupport]

    @Shared Long orgId

    void setupSpec(){
        // super.setupSpec()
        orgId = MockData.org().id
    }

    static Map getNoteParams(){
        return [
            org:[id:205],
            title: 'Todays test note',
            note:[body: 'Todays test note']
        ]
    }

    List createSomeContacts(){
        Contact contact1 = MockData.contact([firstName: "bill"])
        Contact contact2 = MockData.contact([firstName: "bob"])
        [contact1, contact2]
    }

    @Ignore
    void "simple mail creation"() {
        when:
        def params = [orgId: orgId, mail:[body: 'foo']]
        Activity activity = Activity.create(params)

        then:
        activity.note != null
        activity.name == 'foo'
        activity.note.body == 'foo'
    }

    void "simple note creation linkedId"() {
        when:
        def params = [orgId: orgId, note:[body: 'foo'], linkedId: 1, linkedEntity:'Contact']
        Activity activity = Activity.create(params)
        flush()

        then:
        activity.note != null
        activity.name == 'foo'
        activity.note.body == 'foo'
        activity.links[0].linkedId == 1
        activity.links[0].linkedEntity == 'Contact'
    }

    void "simple note creation links list"() {
        when:
        def links = [
            [linkedId: 1, linkedEntity:'Contact'],
            [linkedId: 2, linkedEntity:'Contact']
        ]
        def params = [orgId: orgId, note:[body: 'foo'], links: links]
        Activity activity = Activity.create(params)
        flush()

        then:
        activity.note != null
        activity.name == 'foo'
        activity.note.body == 'foo'
        activity.links[0].linkedId == 1
        activity.links[0].linkedEntity == 'Contact'
        activity.links[1].linkedId == 2

        when:
        // add another activity
        Activity.create(params)
        flush()
        def linkedActs = Activity.repo.query([linkedId: 1, linkedEntity:'Contact']).list()

        then:
        linkedActs.size() == 2

    }

    void "creates note if summary is longer than 255"() {
        when:
        def params = [kind:"Note", org: MockData.org()]
        params.name = RandomStringUtils.randomAlphabetic(300)
        Activity activity = Activity.create(params)

        then:
        activity.note != null
        activity.name.length() == 255
        activity.note.body.length() == 300
        activity.note.body == params.name
    }

    void "update note if exists"() {
        when:
        Org org = Org.get(orgId)
        Activity activity = build(Activity, [org:org])
        activity.persist()
        def params = [kind:"Note", id:activity.id]
        flushAndClear()
        params.name = RandomStringUtils.randomAlphabetic(300)
        Activity updatedActivity = Activity.repo.update(params)

        then:
        updatedActivity.note != null
        updatedActivity.name.length() == 255
        updatedActivity.note.body.length() == 300
        updatedActivity.note.body == params.name
    }

    void "save works on gorm6.1.11 but fails on 6.1.12"(){

        expect:
        def params = [
                orgId: 205, //org id does not exist
                note:[body: 'Todays test note'],
                name: 'will get overriden'
        ]

        Activity act = Activity.create(params)
        flush()
    }

    /*
    * Testcase for saving note without task or attachment
    */
    void "save and remove a simple note"(){
        setup:
        Org org = MockData.org()

        expect:
        org.id != null

        when:
        def params = [
                org    : [id: org.id],
                note   : [body: 'test note'],
                name: '!! should get overriden as it has a note !!'
        ]

        Activity act = Activity.create(params)
        act.persist(flush:true)

        then:
        ActKinds.Note == act.kind
        'test note' == act.name
        'test note' == act.note.body

        when:
        flush()
        Activity activity = Activity.get(act.id)

        then:
        activity
        activity.note
        params.note.body == activity.note.body
        params.note.body == activity.name

        ActKinds.Note == activity.kind
        activity.task == null

        when: "Activity is removed, note also gets removed"
        activity.remove()

        then:
        Activity.get(act.id) == null
        ActivityNote.get(activity.noteId) == null
    }

    void "create note with linkedId"(){
        setup:
        Org org = MockData.org()

        expect:
        org.id != null

        when:
        def params = [
            org    : [id: org.id],
            note   : [body: 'test note'],
            name: '!! should get overriden as it has a note !!'
        ]

        Activity act = Activity.create(params)
        act.persist(flush:true)

        then:
        ActKinds.Note == act.kind
        'test note' == act.name
        'test note' == act.note.body

        when:
        flush()
        Activity activity = Activity.get(act.id)

        then:
        activity
        activity.note
        params.note.body == activity.note.body
        params.note.body == activity.name

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
        Org org = MockData.org()
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
        Org org = Org.get(orgId)
        Activity act = build(Activity, [org:org])
        act.repo.addNote(act, "test body")
        act.persist(flush: true)

        then:
        act.note?.id != null
        def act2 = Activity.findById(act.id)
        act2.note != null
        "test body" == act2.note.body
        "plain" == act2.note.contentType
    }

    void testUpdateSummary_WithNoteKind_AndShortBody(){

        when:
        Org org = Org.get(orgId)
        Activity activity = build(Activity, [org:org, name: 'test summary'])
        activity.repo.addNote(activity, "test body")
        activity.persist()

        then:
        activity.id != null
        //Check summary of saved Activity
        activity.note.body == activity.name
    }

    void testUpdateSummary_WithLongBody(){
        when:
        Org org = Org.get(orgId)
        Activity activity = build(Activity, [org:org, name: 'test summary'])
        //Adding noteBody with more than 255 chars
        String noteBody = "DENIAL 1:  Proof of shipment enclosed.  Order shipped FOB Origin.  Please repay the deduction and address carton shrotage issues with your designated carrier. tt Matt Johnson w/Albuquerque Factory, said not valid sales allowance. LVM to Robert remind. new past due invoices."
        assert noteBody.trim().length() > 255
        activity.repo.addNote(activity, noteBody)
        activity.persist()

        then:
        activity.id != null
        //Check summary of saved Activity
        activity.note.body.trim().substring(0,251)+" ..." == activity.name
        255 == activity.name.length()
    }

    void testUpdateSummary_With255CharsBody(){
        setup:
        Org org = Org.get(orgId)
        Activity activity = build(Activity, [org:org, name: 'test summary'])
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
        activity.note.body.trim() == activity.name
        255 == activity.name.length()
    }

    @Ignore
    def "test removeActivityAttachment"() {
        setup:
        Attachment attachment = build(Attachment) //new Attachment(TestDataJson.buildMap([:], Attachment))
        attachment.location = "foo/bar"
        attachment.persist()
        Org org = Org.get(orgId)
        Activity activity = build(Activity, [org:org]) as Persistable
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
