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

    String action      // Status of the job at the time of the log event
    String appName     // rcm, arApi, gbApi, ...
    String component   // The process the app was called from
    LocalDateTime createdDate  // the date row was created
    BigDecimal controlAmount
    Long controlCount
    Boolean isPrimaryJob = false
    String jobName     // Master unit of work -- importCorrectionPost
    String jobParams   // Params sent to the job.
    Long linkedId    // Long value linking all rows of a specific job together.
    String message     // What needs to be said.
    Integer priority = ERROR_INT
    String source      // deprecated?
    String stackTrace  // Stack trace if it's present.
    Long userId      // The user ID if there is one, otherwise 0.

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
