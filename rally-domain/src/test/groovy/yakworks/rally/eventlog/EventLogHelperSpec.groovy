package yakworks.rally.eventlog

import spock.lang.Ignore
import spock.lang.Specification
import yakworks.testing.gorm.unit.GormHibernateTest

@Ignore
class EventLogHelperSpec extends Specification implements GormHibernateTest  {
    static List entityClasses = [EventLog]

    static final String JOB_NAME  = 'EventLogHelperTests'
    static final String COMPONENT = 'service/method'
    static final String APP_NAME  = 'rally-domain'



    Closure doWithGormBeans() { { ->
        eventLogger(EventLogger)
    }}

    void testSetup_full_map() {
        given:
        EventLogHelper elh = new EventLogHelper(jobName:JOB_NAME, component:COMPONENT, jobParams:[a:'a', b:45], isPrimaryJob:true)

        expect:
        assert elh
        assert elh.eventLogger
        assert elh.component == COMPONENT
        assert elh.jobName == JOB_NAME
        assert elh.isPrimaryJob
        assert elh.jobParams == '[a:a, b:45]'
        assert elh.linkedId
        assert elh.appName == APP_NAME
        //assert elh.userId == 107
    }

    void testSetup_full_string() {
        given:
        EventLogHelper elh = new EventLogHelper(COMPONENT, JOB_NAME, 'a, 45', true)

        expect:
        assert elh
        assert elh.eventLogger
        assert elh.component == COMPONENT
        assert elh.jobName == JOB_NAME
        assert elh.isPrimaryJob
        assert elh.jobParams == 'a, 45'
        assert elh.linkedId
        assert elh.appName == APP_NAME
        //assert elh.userId == 107
    }

    void testSetup_minimal() {
        given:
        EventLogHelper elh = new EventLogHelper(COMPONENT)

        expect:
        assert elh
        assert elh.eventLogger
        assert elh.component == COMPONENT // should come over
        assert elh.jobName == COMPONENT
        assert !elh.isPrimaryJob
        assert !elh.jobParams
        assert elh.linkedId
        assert elh.appName == APP_NAME
        //assert elh.userId == 107
    }

    void testEventLoggerInfo_emptyMap() {
        EventLogHelper elh = new EventLogHelper(COMPONENT)
        elh.debug([:])
    }

    void testEventLoggerInfo_nullMap() {
        EventLogHelper elh = new EventLogHelper(COMPONENT)
        elh.debug(null)
    }

    void testMergeParams_base_nullExtras() {
        given:
        EventLogHelper elh = new EventLogHelper(COMPONENT)
        def result = elh.mergeParams([hello:'world', howdy:'doody'], null)

        expect:
        assert result.size() == 11
        assert result.hello == 'world'
        assert result.howdy == 'doody'
    }

    void testMergeParams_base_emptyExtras() {
        given:
        EventLogHelper elh = new EventLogHelper(COMPONENT)
        def result = elh.mergeParams([hello:'world', howdy:'doody'], [:])

        expect:
        assert result.size() == 11
        assert result.hello == 'world'
        assert result.howdy == 'doody'
    }

    void testMergeParams_base_extras_overlap() {
        given:
        EventLogHelper elh = new EventLogHelper(COMPONENT)
        def result = elh.mergeParams([hello:'world', howdy:'doody'], [howdy:'boys', hardy:'boys'])

        expect:
        assert result.size() == 12
        assert result.hello == 'world'
        assert result.howdy == 'boys'
        assert result.hardy == 'boys'
    }

    void testMergeParams_nullBase_extras() {
        given:
        EventLogHelper elh = new EventLogHelper(COMPONENT)
        def result = elh.mergeParams(null, [howdy:'boys', hardy:'boys'])

        expect:
        assert result.size() == 11
        assert result.howdy == 'boys'
        assert result.hardy == 'boys'
    }

    void testMergeParams_emptyBase_extras() {
        given:
        EventLogHelper elh = new EventLogHelper(COMPONENT)
        def result = elh.mergeParams([:], [howdy:'doody', hardy:'boys'])

        expect:
        assert result.size() == 11
        assert result.howdy == 'doody'
        assert result.hardy == 'boys'
    }

    void testMergeParams_nullBase_nullExtras() {
        given:
        EventLogHelper elh = new EventLogHelper(COMPONENT)
        def result = elh.mergeParams([:], [:])

        expect:
        assert result instanceof Map
        assert result.size() == 9
    }

    void testEventLoggerDebugFinish_noMessage() {
        given:
        EventLogHelper elh = new EventLogHelper(COMPONENT)

        String result = elh.debugFinish()
        flushAndClear()
        def list = EventLog.list()
        assert list.size() == 1
        def row = list[0]

        expect:
        assert row.id
        assert row.appName == APP_NAME
        assert row.component == COMPONENT
        assert row.jobName == COMPONENT
        assert row.jobParams == null
        assert row.priority == EventLog.DEBUG_INT
        assert row.message == "${COMPONENT} finished"
        assert row.action == EventLogHelper.FINISHED
        assert result == row.message
    }

    void testEventLoggerDebugFinish_withMessage() {
        given:
        EventLogHelper elh = new EventLogHelper(COMPONENT, JOB_NAME, [a:'a', b:42], true)
        //jdbcTemplate.update("truncate table EventLog")
        flushAndClear()
        String result = elh.debugFinish('blah blah blah')
        flushAndClear()
        def list = EventLog.list()

        expect:
        assert list.size() == 1
        def row = list[0]
        assert row.id
        assert row.appName == APP_NAME
        assert row.component == COMPONENT
        assert row.jobName == JOB_NAME
        assert row.jobParams == '[a:a, b:42]'
        assert row.isPrimaryJob
        assert row.priority == EventLog.DEBUG_INT
        assert row.message == "${JOB_NAME} finished: blah blah blah"
        assert row.action == EventLogHelper.FINISHED
        assert result == row.message
    }

    void testEventLoggerDebugStart() {
        given:
        EventLog.deleteAll()
        flushAndClear()
        EventLog.list().size() == 0
        EventLogHelper elh = new EventLogHelper(COMPONENT, JOB_NAME, 'a, 42')
        elh.debugStart()
        flushAndClear()
        def list = EventLog.list()

        expect:
        assert list.size() == 1
        def row = list[0]
        assert row.id
        assert row.appName == APP_NAME
        assert row.component == COMPONENT
        assert row.jobName == JOB_NAME
        assert row.jobParams == 'a, 42'
        assert !row.isPrimaryJob
        assert row.priority == EventLog.DEBUG_INT
        assert row.message == "${JOB_NAME} started"
        assert row.action == EventLogHelper.STARTED
    }

    void testEventLoggerInfoFinish_noMessage() {
        given:
        EventLogHelper elh = new EventLogHelper(COMPONENT)
        //jdbcTemplate.update("truncate table EventLog")
        flushAndClear()
        String result = elh.infoFinish()
        flushAndClear()
        def list = EventLog.list()

        expect:
        assert list.size() == 1
        def row = list[0]
        assert row.id
        assert row.appName == APP_NAME
        assert row.component == COMPONENT
        assert row.jobName == COMPONENT
        assert row.priority == EventLog.INFO_INT
        assert row.message == "${COMPONENT} finished"
        assert row.action == EventLogHelper.FINISHED
        assert result == row.message
    }

    void testEventLoggerInfoStart() {
        given:
        EventLogHelper elh = new EventLogHelper(COMPONENT)
        //jdbcTemplate.update("truncate table EventLog")
        flushAndClear()
        elh.infoStart()
        flushAndClear()
        def list = EventLog.list()

        expect:
        assert list.size() == 1
        def row = list[0]
        assert row.id
        assert row.appName == APP_NAME
        assert row.component == COMPONENT
        assert row.jobName == COMPONENT
        assert row.priority == EventLog.INFO_INT
        assert row.message == "${COMPONENT} started"
        assert row.action == EventLogHelper.STARTED
    }

    void testEventLoggerInfoFinish_withMessage() {
        given:
        EventLogHelper elh = new EventLogHelper(COMPONENT)
        //jdbcTemplate.update("truncate table EventLog")
        flushAndClear()
        String result = elh.infoFinish('blah blah blah')
        flushAndClear()
        def list = EventLog.list()

        expect:
        assert list.size() == 1
        def row = list[0]
        assert row.id
        assert row.appName == APP_NAME
        assert row.component == COMPONENT
        assert row.jobName == COMPONENT
        assert row.priority == EventLog.INFO_INT
        assert row.message == "${COMPONENT} finished: blah blah blah"
        assert row.action == EventLogHelper.FINISHED
        assert result == row.message
    }

    void testEventLoggerErrorFinish_withMessageAndError() {
        given:
        EventLogHelper elh = new EventLogHelper(COMPONENT)
        //jdbcTemplate.update("truncate table EventLog")
        flushAndClear()
        Exception e = new Exception('hell, world!')
        String result = elh.errorFinish(e, 'blah blah blah')
        flushAndClear()
        def list = EventLog.list()

        expect:
        assert list.size() == 1
        def row = list[0]
        assert row.id
        assert row.appName == APP_NAME
        assert row.component == COMPONENT
        assert row.jobName == COMPONENT
        assert row.priority == EventLog.ERROR_INT
        assert row.message == "${COMPONENT} finished abnormally: blah blah blah"
        assert row.stackTrace.contains(e.message)
        assert row.action == EventLogHelper.ERROR
        assert result == row.message
    }

    void testEventLoggerErrorFinish_noMessageWithError() {
        given:
        EventLogHelper elh = new EventLogHelper(COMPONENT)
        //jdbcTemplate.update("truncate table EventLog")
        flush()
        Exception e = new Exception('hell, world!')
        String result = elh.errorFinish(e)
        flush()
        def list = EventLog.list()

        expect:
        assert list.size() == 1
        def row = list[0]
        assert row.id
        assert row.appName == APP_NAME
        assert row.component == COMPONENT
        assert row.jobName == COMPONENT
        assert row.priority == EventLog.ERROR_INT
        assert row.message == "${COMPONENT} finished abnormally: hell, world!"
        assert row.stackTrace.contains(e.message)
        assert row.action == EventLogHelper.ERROR
        assert result == row.message
    }

    void testEventLoggerErrorFinish_withMessageNoError() {
        given:
        EventLogHelper elh = new EventLogHelper(COMPONENT)
        //jdbcTemplate.update("truncate table EventLog")
        flushAndClear()
        String result = elh.errorFinish('blah blah blah')
        flushAndClear()
        def list = EventLog.list()

        expect:
        assert list.size() == 1
        def row = list[0]
        assert row.id
        assert row.appName == APP_NAME
        assert row.component == COMPONENT
        assert row.jobName == COMPONENT
        assert row.priority == EventLog.ERROR_INT
        assert row.message == "${COMPONENT} finished abnormally: blah blah blah"
        assert !row.stackTrace
        assert row.action == EventLogHelper.ERROR
        assert result == row.message
    }

    void testEventLoggerErrorFinish_noMessageNoError() {
        given:
        EventLogHelper elh = new EventLogHelper(COMPONENT)
        //jdbcTemplate.update("truncate table EventLog")
        flushAndClear()
        String result = elh.errorFinish()
        flushAndClear()
        def list = EventLog.list()

        expect:
        assert list.size() == 1
        def row = list[0]
        assert row.id
        assert row.appName == APP_NAME
        assert row.component == COMPONENT
        assert row.jobName == COMPONENT
        assert row.priority == EventLog.ERROR_INT
        assert row.message == "${COMPONENT} finished abnormally with no message."
        assert !row.stackTrace
        assert row.action == EventLogHelper.ERROR
        assert result == row.message
    }
}
