package yakworks.rally.eventlog

import java.time.LocalDateTime

import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import yakworks.testing.gorm.unit.DataRepoTest
import yakworks.testing.gorm.unit.GormHibernateTest

class EventLogSpec extends Specification implements GormHibernateTest {
    static List entityClasses = [EventLog]
    static Map springBeans = [eventLogger: EventLogger]

    @Autowired EventLogger eventLogger

    //auto runs DomainRepoCrudSpec tests
    static final MESSAGE = 'EventLoggerTests'
    static final exText = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."+
            "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?"+
            "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."+
            "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?"+"Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."+
            "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?"+
            "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."+
            "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?"+
            "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."+
            "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?"+
            "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."+
            "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?"+
            "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."+
            "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?"+
            "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."+
            "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?"

    // void setupSpec(){
    //     defineBeans({
    //         eventLogger(EventLogger)
    //     })
    //     assert config
    // }

    @Override
    Closure doWithConfig() {
        { cfg ->
            cfg.nine.eventLog.purgeDays = 2
        }
    }

    void deleteAll(){
        EventLog.query([:]).deleteAll()
        flushAndClear()
        assert EventLog.count() == 0
    }

    void testError_Exception() {

        when:
        def beforeList = EventLog.findAllByMessage(MESSAGE)

        then:
        0 == beforeList.size()

        when:
        def exception = new Exception('Sample exception to test this feature.')
        eventLogger.error(MESSAGE, exception)
        def afterList = EventLog.findAllByMessage(MESSAGE)

        then:
        1 == afterList.size()

        when:
        def last = afterList.last()

        then:
        MESSAGE == last.message
        EventLog.ERROR_INT == last.priority

        when:
        def stack = EventLogger.convertStackTrace(exception)

        then:
        stack != null
        stack == last.stackTrace

    }

    void testWarn() {
        setup:
        deleteAll()

        when:
        def beforeList = EventLog.findAllByMessage(MESSAGE)

        then:
        0 == beforeList.size()

        when:
        eventLogger.warn(MESSAGE)
        def afterList = EventLog.findAllByMessage(MESSAGE)

        then:
        1 == afterList.size()
        MESSAGE == afterList.last().message
        EventLog.WARN_INT == afterList.last().priority

    }

    void testInfo() {
        setup:
        deleteAll()

        when:
        def beforeList = EventLog.findAllByMessage(MESSAGE)

        then:
        0 == beforeList.size()

        when:
        eventLogger.info(MESSAGE)
        def afterList = EventLog.findAllByMessage(MESSAGE)

        then:
        1 == afterList.size()
        MESSAGE == afterList.last().message
        EventLog.INFO_INT == afterList.last().priority

    }

    void testLogMap() {
        setup:
        deleteAll()

        when:
        def COMPONENT = 'EventLogTests'
        def ACTION = 'logMap'
        def beforeList = EventLog.findAllByMessage(MESSAGE)

        then:
        0 == beforeList.size()

        when:
        def exception = new Exception(MESSAGE)
        eventLogger.log(message:MESSAGE, exception:exception, component:COMPONENT, action:ACTION)
        flushAndClear()
        def afterList = EventLog.findAllByMessage(MESSAGE)

        then:
        1 == afterList.size()

        when:
        def last = afterList.last()

        then:
        MESSAGE == last.message
        COMPONENT == last.component
        ACTION == last.action

        when:
        afterList.each {
            it.delete()
        }
        def stack = EventLogger.convertStackTrace(exception)

        then:
        stack != null
        stack == last.stackTrace

    }

    void testError() {
        setup:
        deleteAll()

        when:
        def beforeList = EventLog.findAllByMessage(MESSAGE)

        then:
        0 == beforeList.size()

        when:
        eventLogger.error(MESSAGE)
        def afterList = EventLog.findAllByMessage(MESSAGE)

        then:
        1 == afterList.size()
        MESSAGE == afterList.last().message
        EventLog.ERROR_INT == afterList.last().priority

    }

    void testPurgeEvents() {
        setup:
        deleteAll()

        when:
        EventLog eventLog = new EventLog([
            action:'testAct1',component:'testComp1',jobName:'testName1',message:'testMsg1',
            priority:'3',userId:9
        ])
        eventLog.save(flush:true)
        eventLog.createdDate = LocalDateTime.now().minusDays(100)
        eventLog.save(flush:true)

        eventLog = new EventLog([
            action:'testAct2',component:'testComp2', jobName:'testName2',message:'testMsg2',
            priority:'3', userId:9
        ])
        eventLog.createdDate= LocalDateTime.now()
        eventLog.save(flush:true)

        then:
        EventLog.list().size() == 2

        when:
        eventLogger.purgeEvents()
        flush()

        then:
        EventLog.count() == 1

    }

    void testError_Exception_Long() {
        setup:
        deleteAll()

        when:
        def exception = new Exception(exText)
        eventLogger.error(exText, exception)
        def afterList = EventLog.findAllByMessage(exText.substring(0,EventLogger.MAX_MESSAGE_SIZE))

        then:
        1 == afterList.size()

        when:
        def last = afterList.last()

        then:
        exText.substring(0,EventLogger.MAX_MESSAGE_SIZE) == last.message

        when:
        EventLog.ERROR_INT == last.priority
        def stack = EventLogger.convertStackTrace(exception)

        then:
        stack != null
        stack.substring(0, Math.min(EventLogger.MAX_MESSAGE_SIZE, stack.length())) ==  last.stackTrace

    }

    void testConvertStackTrace() {

        when:
        def exception = new Exception(MESSAGE)
        def output = EventLogger.convertStackTrace(exception)

        then:
        output.contains(MESSAGE)
        output.contains('java.lang.Exception')
    }
}
