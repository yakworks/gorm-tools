/*
* Copyright 2023 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job


import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal

import groovy.transform.CompileStatic

import org.springframework.scheduling.support.CronExpression

import yakworks.api.problem.ProblemResult
import yakworks.rally.config.MaintenanceProps

/**
 * Tools to help parse a cron expression and see if we are inside a maintenance window
 */
@CompileStatic
class MaintWindowUtil {

    static boolean check(MaintenanceProps mprops){
        return check(mprops.crons, mprops.timeZone?.toZoneId())
    }

    static boolean check(List<String> cronList, ZoneId zoneId = null){
        return checkWindows(cronList, currentTime(zoneId))
    }

    /**
     * checks to see if the date falls inisde the list of cron expresions.
     *
     * @param cronList the list to crons to see if the curDate falls inside. If empty or null then it returns true
     * @param curDate the date to check against. If null then it retuns true.
     * @return true if its good, will never return false as it throws error
     */
    static <T extends Temporal & Comparable<? super T>> boolean checkWindows(List<String> cronList, T curDate){
        cronList?.each { String cronString ->
            long secondsToNextRun = secondsToNextRun(cronString, curDate)
            //string should be like "* * 14,15 * * MON-FRI", so it runs every seconds.
            // if we are at 1 second then we know we are in the window
            if (secondsToNextRun <= 1) {
                //throw problem
                // XXX add header for the retry-after.
                throw new ProblemResult().title("System Maintenance. No Jobs Can be Run. See Retry-After Header.").status(503).toException()
            }
        }
        return true
    }

    static <T extends Temporal & Comparable<? super T>> long secondsToNextRun(String cronString, T curDate){
        //Every second and minute should have * in those fields as we are only looking at hours
        var expression = CronExpression.parse(cronString)
        Temporal nextRun = expression.next(curDate)
        long secDif = ChronoUnit.SECONDS.between(curDate, nextRun);
        return secDif
    }

    /** Uses timezone in MaintenanceProps to get the current time */
    static LocalDateTime currentTime(ZoneId zoneId){
        LocalDateTime timeToUse = zoneId ? LocalDateTime.now(zoneId) : LocalDateTime.now()
        return timeToUse
    }

}
