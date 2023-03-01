/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.eventlog

import java.time.LocalDateTime

import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.gorm.annotation.Entity

@Entity
@GrailsCompileStatic
class EventLog implements RepoEntity<EventLog>, Serializable {
    public final static int FATAL_INT = 50000
    public final static int ERROR_INT = 40000
    public final static int WARN_INT = 30000
    public final static int INFO_INT = 20000
    public final static int DEBUG_INT = 10000

    /** State of the job at the time of the log event */
    String action //FIXME make this anEnum and rename to state
    /** rcm, arApi, gbApi, ... */
    String appName
    /** The service.method this was called from. just a decription to tie it down further */
    String component
    /** the date row was created */
    LocalDateTime createdDate

    //XXX remove from table and get in sync
    // BigDecimal controlAmount
    // Long controlCount

    /** if this is a log for primaryJob */
    Boolean isPrimaryJob = false //FIXME remove?
    /** Master unit of work -- importCorrectionPost */
    String jobName
    /**  Params sent to the job.*/
    String jobParams
    /** Long value linking all rows of a specific job together. */
    Long linkedId //FIXME if we are going to keep this then need both linkedId and linkedEntity
    /**  What needs to be said. */
    String message
    /** the priority lever */
    Integer priority = ERROR_INT

    //String source      // deprecated?
    /** Stack trace dump, not common to use*/
    String stackTrace
    /** The user ID if whatever is logging this is from specifc user */
    Long userId

    static mapping = {
        //cache true
        // table 'EventLog'
        id generator: 'identity'
    }

    static constraints = {
        createdDate nullable:false, display:false, editable:false, bindable:false
    }

    //update the summary on save
    def beforeInsert() {
        if(!createdDate) createdDate = LocalDateTime.now()
    }

    void beforeValidate() {
        if(!createdDate) createdDate = LocalDateTime.now()
    }

    static transients = ['priorityName']

    /** Gets the priority in human-readable form. Not persistable */
    String getPriorityName() {
        switch (priority) {
            case FATAL_INT: 'fatal'; break
            case ERROR_INT: 'error'; break
            case WARN_INT: 'warning'; break
            case INFO_INT: 'info'; break
            case DEBUG_INT: 'debug'; break
        }
    }

    String toString() {
        return message
    }
}
