/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.lang

import java.text.DateFormat
import java.text.DateFormatSymbols
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Year
import java.time.ZoneId
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor

import groovy.transform.CompileStatic

/**
 * custom manipulations with dates.
 * (e.g. to get a number of days between dates or to get last day of month, etc)
 */
@CompileStatic
@SuppressWarnings(['MethodCount'])
class DateUtil {


    /**
     * Returns the first day of the current week and sets time to midnight.
     */
    // static Date getFirstDayOfWeek() {
    //     Calendar startDate = getCurrentCalendarInstance()
    //     int dayOfWeek = startDate.get(Calendar.DAY_OF_WEEK)
    //     while (dayOfWeek != Calendar.MONDAY) {
    //         startDate.add(Calendar.DATE, -1)
    //         dayOfWeek = startDate.get(Calendar.DAY_OF_WEEK)
    //     }
    //     return setTimeAsOfMidnight(startDate).getTime()
    // }

    /**
     * Returns the last day of the current week and sets time to before midnight (23:59:59).
     */
    // static Date getLastDayOfWeek() {
    //     Calendar endDate = getCurrentCalendarInstance()
    //     int dayOfWeek = endDate.get(Calendar.DAY_OF_WEEK)
    //     while (dayOfWeek != Calendar.SUNDAY) {
    //         endDate.add(Calendar.DATE, 1)
    //         dayOfWeek = endDate.get(Calendar.DAY_OF_WEEK)
    //     }
    //     return setTimeBeforeMidnight(endDate).getTime()
    // }


    /**
     * Returns a Calendar instance for a given date.
     *
     * @param date a date
     * @return a Calendar instance for a given date
     */
    static Calendar getCurrentCalendarInstance() {
        getCalendarInstanceByDate(new Date())
    }

    /**
     * Returns a Calendar instance for a given date.
     *
     * @param date a date
     * @return a Calendar instance for a given date
     */
    static Calendar getCalendarInstanceByDate(Date date) {
        Calendar calendarDate = Calendar.getInstance()
        calendarDate.setTime(date)
        return calendarDate
    }

    /**
     * Returns the first letter of a month.
     * expected format: YYYYMM
     *
     * @param periodToPost a string which contains date in format YYYYMM
     * @return the first letter of a specified month
     */
    static String getMonthLetter(String periodToPost) {
        String monthLetter
        if (periodToPost && periodToPost.length() >= 5) {
            String month = periodToPost.substring(4, 6)
            monthLetter = month.isInteger() ? getMonthLetterByNum(Integer.parseInt(month)) : '?'
        } else {
            monthLetter = '?'
        }

        return monthLetter
    }

    /**
     * Returns the first letter(uppercase) of a specified month.
     *
     * @param monthNumber number of a month
     * @return the first letter of a specified month
     */
    @SuppressWarnings('ExplicitCallToGetAtMethod')
    static String getMonthLetterByNum(int monthNumber) {
        String[] monthNames = new DateFormatSymbols().getMonths()
        monthNumber in (1..12) ? monthNames[monthNumber - 1].getAt(0) : '?'
    }

    /**
     * Converts a date to a specified format.
     *
     * @param strDt a date as a string
     * @param format a format
     * @return a date
     */
    @SuppressWarnings('EmptyCatchBlock')
    static Date convertStringToDateTime(String strDt, String format) {
        DateFormat df = new SimpleDateFormat(format, Locale.US)
        Date dtTmp = null
        try {
            dtTmp = df.parse(strDt)
        } catch (ParseException e) {
            //e.printStackTrace()
        }
        return dtTmp
    }

    /**
     * Returns the year of the date specified.
     */
    static int getYearOf(Date date) {
        Calendar gc = new GregorianCalendar()
        gc.setTime(date)
        return gc.get(Calendar.YEAR)
    }

    /**
     * Returns the year of the date specified.
     */
    static int getYearOf(TemporalAccessor date) {
        Year.from(date).getValue()
    }

    /**
     * Calculates a number of hours between now and the specified date.
     *
     * @param date a date for which to calculate the difference
     * @return a number of hours between now and the specified date.
     */
    static long getDateDifference_inHours(Date date) {
        Calendar lastModifiedDateCalendar = getCalendarInstanceByDate(date)
        long diff = getCurrentCalendarInstance().getTimeInMillis() - lastModifiedDateCalendar.getTimeInMillis()
        return Math.abs(diff) / (60 * 60 * 1000) as long
    }

    static LocalDate toLocalDate(Date date, ZoneId zoneId = ZoneId.systemDefault()) {
        if(!date) return null
        return Instant.ofEpochMilli(date.getTime()).atZone(zoneId).toLocalDate()
    }

    static LocalDateTime toLocalDateTime(Date date, ZoneId zoneId = ZoneId.systemDefault()) {
        if(!date) return null
        return Instant.ofEpochMilli(date.getTime()).atZone(zoneId).toLocalDateTime()
    }

    static Date fromLocalDate(LocalDate localDate, ZoneId zoneId = ZoneId.systemDefault()) {
        if(!localDate) return null
        return Date.from(localDate.atStartOfDay(zoneId).toInstant())
    }

    static Date fromLocalDateTime(LocalDateTime localDate, ZoneId zoneId = ZoneId.systemDefault()) {
        if(!localDate) return null
        return Date.from(localDate.atZone(zoneId).toInstant())
    }

    /**
     * takes a date, localDate or localDateTime and returns is as date, using system zone
     * used in spots like export to excel to just a date that compatible
     */
    static Date convertToDate(Object temporal) {
        if(!temporal) return null
        if(temporal instanceof Date) return temporal
        if(temporal instanceof LocalDate) return fromLocalDate(temporal)
        if(temporal instanceof LocalDateTime) return fromLocalDateTime(temporal)
        return null
    }

    static boolean isSameDay(Date date1, Date date2){
        def ld1 = LocalDateTime.ofInstant(date1.toInstant(), ZoneId.systemDefault()).toLocalDate()
        def ld2 = LocalDateTime.ofInstant(date2.toInstant(), ZoneId.systemDefault()).toLocalDate()
        return ld1 == ld2
    }

}
