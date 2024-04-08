/*
* Copyright 2023 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
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

    static boolean checkWindows(MaintenanceProps mprops){
        LocalDateTime timeToUse = currentTime(mprops)
        return checkWindows(mprops, timeToUse)
    }

    static boolean checkWindows(MaintenanceProps mprops, LocalDateTime timeToUse){
        List<String> cronList = mprops.crons
        return checkWindows(cronList, timeToUse)
    }

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
        //should be every second and minute with * in those fields
        //var expression = CronExpression.parse("* * 14,15 * * MON-FRI")
        //var curDate = LocalDateTime.parse("2023-09-20T13:59:59")//a wednesday
        var expression = CronExpression.parse(cronString)
        Temporal nextRun = expression.next(curDate)
        long secDif = ChronoUnit.SECONDS.between(curDate, nextRun);
        return secDif
    }

    static LocalDateTime currentTime(MaintenanceProps mprops){
        TimeZone timeZone = mprops.timeZone
        LocalDateTime timeToUse = timeZone ? LocalDateTime.now(timeZone.toZoneId()) : LocalDateTime.now()
        return timeToUse
    }

    static LocalDateTime toLocalDateTimeZone(ZonedDateTime zonedDateTime, ZoneId toZoneId){
        return zonedDateTime
            .withZoneSameInstant(toZoneId)
            .toLocalDateTime()
    }

    static LocalDateTime toLocalDateTimeZone(LocalDateTime localDateTime, ZoneId fromZoneId, ZoneId toZoneId){
        return localDateTime.atZone(fromZoneId)
            .withZoneSameInstant(toZoneId)
            .toLocalDateTime()
    }
}
