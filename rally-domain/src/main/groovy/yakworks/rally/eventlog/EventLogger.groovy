/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.eventlog

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import groovy.util.logging.Slf4j

import org.grails.datastore.mapping.query.api.Criteria
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation

import gorm.tools.beans.Pager
import grails.compiler.GrailsCompileStatic
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.util.Metadata

@Slf4j
@Service @Lazy
@CompileStatic
class EventLogger {

    final static int MAX_MESSAGE_SIZE = 3998

    @Value('${nine.eventLog.purgeDays:0}')
    int purgeDays

    @Value('${nine.eventLog.searchDays:7}')
    int searchDays

    @Autowired
    GrailsApplication grailsApplication

    EventLog error(String message) {
        log(message: message, priority: EventLog.ERROR_INT)
    }

    EventLog error(String message, Throwable throwable) {
        log(message: message, throwable: throwable, priority: EventLog.ERROR_INT)
    }

    EventLog error(Map params) {
        params['priority'] = EventLog.ERROR_INT
        log(params)
    }

    EventLog info(String message) {
        log(message: message, priority: EventLog.INFO_INT)
    }

    EventLog info(Map params) {
        params['priority'] = EventLog.INFO_INT
        log(params)
    }

    EventLog warn(String message) {
        log(message: message, priority: EventLog.WARN_INT)
    }

    EventLog warn(Map params) {
        params['priority'] = EventLog.WARN_INT
        log(params)
    }

    /** This needs to be the sole caller for logTransactional.  We MUST have a try-catch here because if logTransactional blows
     * up we would otherwise have no idea there was a problem.
     */
    EventLog log(Map params) {
        try {
            return logTransactional(params)
        } catch (e) {
            log.error(e.message, e)
        }
    }

    //Write the log to database in a seperate independent transaction.
    @GrailsCompileStatic
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private EventLog logTransactional(Map params) {
        EventLog row = new EventLog()
        row.bind(params)
        row.appName = row.appName ?: "${Metadata.current.'app.name'}"

        String message = params['message']
        if (message?.size() > MAX_MESSAGE_SIZE)
            row.message = message.substring(0, MAX_MESSAGE_SIZE)
        if ((!row.stackTrace) && params.exception) {
            row.stackTrace = convertStackTrace((Throwable) params.exception)
        } else if (params.throwable) {
            row.stackTrace = convertStackTrace((Throwable) params.throwable)
        }
        row.persist(flush: true)
    }

    static String convertStackTrace(Throwable throwable) {
        Writer writer = new StringWriter()
        PrintWriter printer = new PrintWriter(writer)
        throwable.printStackTrace(printer)

        printer.flush()
        writer.flush()

        String str = writer

        if (str.length() > MAX_MESSAGE_SIZE) {
            return str.substring(0, MAX_MESSAGE_SIZE)
        }

        return str
    }

    List<EventLog> getEventList(boolean isSummaryOnly = false) {
        return getEventList(isSummaryOnly: isSummaryOnly)
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    List<EventLog> getEventList(Map params) {
        Pager pager = new Pager(params)
        Criteria criteria = EventLog.createCriteria()
        Date searchDate = new Date() - searchDays
        List<EventLog> events = criteria.list(max: pager.max, offset: pager.offset) {
            if (searchDays > 0) {
                gt('createdDate', searchDate)
            }
            if (params.isSummaryOnly) {
                eq('action', 'summary')
            }
            order('createdDate', 'desc')
        }
        return events
    }

    Long eventCount() {
        return getEventList().size()
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    void purgeEvents() {
        if (purgeDays > 0) {
            Date cutoff = new Date() - purgeDays
            cutoff.clearTime()
            if (cutoff) {
                EventLog.createCriteria().list {
                    le "createdDate", cutoff
                }*.delete()
                        //findAllByCreatedDateLessThan(cutoff)*.delete()
                //EventLog.executeUpdate("delete EventLog where createdDate < :cutoff", [cutoff:cutoff])
            }
        }
    }
}
