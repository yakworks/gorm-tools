/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.eventlog

import groovy.transform.CompileDynamic

import grails.compiler.GrailsCompileStatic
import grails.util.Holders

@GrailsCompileStatic
class EventLogHelper {
    static final String FINISHED = 'finished'
    static final String STARTED = 'started'
    static final String ERROR = 'error'
    static final String WARNING = 'warning'
    static final String TERMINATED = 'terminated'

    //SecService secService
    EventLogger eventLogger

    String appName       // Name of the application
    String component     // The service/method called
    Boolean isPrimaryJob  // True if the customer wants to hear about this job every day.
    String jobName       // The name of the master unit of work.
    Long linkedId      // Timestamp to tie all calls to this helper together into a single process.
    String jobParams     // The params passed into the  process
    Long userId        // The current user

    /** Constructs the helper in the case where params should have jobName.
     * @param component The service/method called
     * @param jobParams The parameters used in the call
     * @param isPrimaryJob true if this is a job the customer is interested in hearing about every day.
     * @return A helper with all this plus a userid and appName and a linkedId already generated.
     */
    EventLogHelper(String component, Map jobParams, Boolean isPrimaryJob = false) {
        this(component, component, jobParams as String, isPrimaryJob)
        if (jobParams?.jobName) {
            this.jobName = (jobParams.jobName) as String
        }
    }

    /** Constructs the helper.
     * @param jobName The master unit of work as recognized by a customer
     * @param component The service/method called
     * @param jobParams The parameters used in the call
     * @param isPrimaryJob true if this is a job the customer is interested in hearing about every day.
     * @return A helper with all this plus a userid and appName and a linkedId already generated.
     */
    EventLogHelper(String component, String jobName, Map jobParams, Boolean isPrimaryJob = false) {
        this(component, jobName as String, jobParams as String, isPrimaryJob)
        if (!jobName && (jobParams?.jobName)) {
            this.jobName = (jobParams.jobName) as String
        }
    }

    /** Constructs the helper.
     * @param jobName The master unit of work as recognized by a customer
     * @param component The service/method called
     * @param jobParams The parameters used in the call
     * @param isPrimaryJob true if this is a job the customer is interested in hearing about every day.
     * @return A helper with all this plus a userid and appName and a linkedId already generated.
     */
    EventLogHelper(String component, String jobName = null, String jobParams = null, Boolean isPrimaryJob = false) {
        //secService = (SecService) Holders.applicationContext.getBean('secService')
        eventLogger = (EventLogger) Holders.applicationContext.getBean('eventLogger')
        this.appName = "${Holders.grailsApplication.config.getProperty("info.app.name", String)}"
        this.component = component
        this.jobName = jobName ? (jobName.replaceAll('.groovy', '')) : component
        this.isPrimaryJob = isPrimaryJob
        this.jobParams = jobParams
        this.linkedId = new Date().getTime() // Ties all calls to this helper together
    }

    EventLogHelper(Map params) {
        this((String) (params.component), (String) (params.jobName), (params.jobParams as String), (Boolean) (params.isPrimaryJob))
    }

/*    *//** If we're not logged in, login as system user. *//*
    void checkLogin() {
        if (!secService.isLoggedIn()) {
            SecService.loginAsSystemUser()
        }
        this.userId = secService.userId
    }*/

    @SuppressWarnings('ConfusingMethodName')
    String error(Map params) {
        Map p = mergeParams([action: ERROR], params)
        eventLogger.error(p)
        return p.message
    }

    String warn(Map params) {
        Map p = mergeParams([action: WARNING], params)
        eventLogger.warn(p)
        return p.message
    }

    String info(Map params) {
        Map p = mergeParams([:], params)
        eventLogger.info(p)
        return p.message
    }

    String debug(Map params) {
        Map p = mergeParams([priority: EventLog.DEBUG_INT], params)
        eventLogger.log(p)
        return p.message
    }

    /** getFinishMessage calculates the message based on available arguments and initial ScriptUtils
     * configuration. This script exists so a controller can get the same message that went to eventLogger
     * and log4j.
     * @param params A map with zero or more of:
     *     exception:  A Throwable (all we really need is a message property here)
     *     message:    An overriding message passed in.
     *     isError:   If true then the call will be treated like an exception was thrown even if none is passed in.
     * @return a String containing the same message that would be in your eventLogger call.
     */
    @CompileDynamic
    String getFinishMessage(Map params) {
        Map args = params ?: [:] // safety in case they passed in an explicit null object
        String suffix = (args.exception || args.isError) ? ' abnormally' : ''
        String msg = "${jobName} finished${suffix}"

        if (args.message) {
            msg += ": ${args.message}"
        } else {
            if (args.exception) {
                msg += ": ${args.exception.message}"
            } else {
                if (args.isError) msg += " with no message."
            }
        }
        return msg
        //return "${msg} -- took ${getExecTimeMessage()}"
    }

    String getStartMessage(String message) {
        String msg = message ? ": ${message}" : ''
        return "${jobName} started${msg}"
    }

    /** debugStart is a convenience method that prints that the script is starting.
     *  All information necessary to generate the message already exists in the ScriptUtils object.
     *  This method is for convenience and consistency.
     */
    String debugStart(String message) {
        debug(action: STARTED, message: getStartMessage(message))
    }

    /** debugFinish is a convenience method that prints that the script is finished.
     * All information necessary to generate the message already exists in the ScriptUtils object.
     * This method is for convenience and consistency.
     * @param message An optional string to be appended to the message.
     * @return the message sent to log4j and eventLogger
     */
    String debugFinish(String message = null) {
        debug(action: FINISHED, message: getFinishMessage(message: message))
    }

    /** infoStart is a convenience method that prints that the script is starting.
     *  All information necessary to generate the message already exists in the ScriptUtils object.
     *  This method is for convenience and consistency.
     */
    String infoStart(String message) {
        info(action: STARTED, message: getStartMessage(message))
    }

    /** infoFinish is a convenience method that prints that the script is finished.
     * All information necessary to generate the message already exists in the ScriptUtils object.
     * This method is for convenience and consistency.
     * @param message An optional string to be appended to the message.
     * @return the message sent to log4j and eventLogger
     */
    String infoFinish(String message = null) {
        info(action: FINISHED, message: getFinishMessage(message: message))
    }

    /** warnFinish is a convenience method that prints that the script is finished.
     * All information necessary to generate the message already exists in the ScriptUtils object.
     * This method is for convenience and consistency.
     * @param message An optional string to be appended to the message.
     * @return the message sent to log4j and eventLogger
     */
    String warnFinish(String message = null) {
        warn(action: WARNING, message: getFinishMessage(message: message))
    }

    /** warnFinish is a convenience method that prints that the script is finished.
     * All information necessary to generate the message already exists in the ScriptUtils object.
     * This method is for convenience and consistency.
     * @param message An optional string to be appended to the message.
     * @return the message sent to log4j and eventLogger
     */
    String terminate(String message = null) {
        warn(action: TERMINATED, message: getFinishMessage(message: message))
    }

    /** errorFinish is a convenience method that prints that the script is finished.
     * All information necessary to generate the message already exists in the ScriptUtils object.
     * This method is for convenience and consistency.
     * @param err An optional Throwable related to the call.
     * @param message An optional string to be appended to the message.
     * @return the message sent to log4j and eventLogger
     */
    String errorFinish(def ... args) {
        Throwable err = null
        String msg = null
        args?.each {
            if (it instanceof Throwable) {
                err = (Throwable) it
            } else if (it instanceof String) {
                msg = it
            }
        }
        error(action: ERROR, throwable: err, message: getFinishMessage(exception: err, message: msg, isError: true))
    }

    //@CompileDynamic
    String getExecTimeMessage() {
        BigDecimal time = (new Date().getTime() - linkedId) / 1000
        if (time < 60.00) {
            return "${time} seconds"
        }

        return (time < 3600.00) ? "${time / 60} minutes" : "${time / 3600} hours"

    }

    /** mergeParams safely merges three maps:  Default values, method defaults and passed-in parameters.
     * @param base A Map with method-default values.
     * @param extras A map with other values, duplicates here override values in base.
     * @param a Map containing nothing, or the merged combination of base and extras.
     */
    @CompileDynamic
    Map mergeParams(Map base, Map extras) {
        Map r = [appName : appName, component: component, isPrimaryJob: isPrimaryJob, jobName: jobName,
                 linkedId: linkedId, jobParams: jobParams, userId: userId, action: '...', message: ''
        ]
        base?.each { key, value ->
            r[key] = value
        }
        extras?.each { key, value ->
            r[key] = value
        }
        return r
    }
}
