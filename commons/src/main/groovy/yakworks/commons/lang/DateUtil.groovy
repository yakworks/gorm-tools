/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.lang

import java.text.DateFormat
import java.text.DateFormatSymbols
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Year
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAccessor

import groovy.time.TimeCategory
import groovy.time.TimeDuration
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

/**
 * custom manipulations with dates.
 * (e.g. to get a number of days between dates or to get last day of month, etc)
 */
@CompileStatic
@SuppressWarnings(['MethodCount'])
class DateUtil {

    /**
     * Returns the next month for current date and sets the day number to 1.
     *
     * @return the next month
     */
    static Date getNextMonth() {
        return shiftCurrentDateByMonths(1)
    }

    /**
     * Returns the previous month for current date and sets the day number to 1.
     *
     * @return the previous month
     */
    static Date getPreviousMonth() {
        return shiftCurrentDateByMonths(-1)
    }

    //looks like those are not used
    @Deprecated
    static Date getTwoMonthsBack() {
        return shiftCurrentDateByMonths(-2)
    }

    @Deprecated
    static Date getThreeMonthsBack() {
        return shiftCurrentDateByMonths(-3)
    }

    @Deprecated
    static Date getFourMonthsBack() {
        return shiftCurrentDateByMonths(-4)
    }

    /**
     * Shifts the current date by specified number of months
     * and sets current day of month to 1.
     *
     * @param months number of months to shift
     * @return a date which shifted on specified number of months from now
     */
    static Date shiftCurrentDateByMonths(int months) {
        Calendar month = getCurrentCalendarInstance()
        month.set(Calendar.DATE, 1)
        month.add(Calendar.MONTH, months)
        return month.getTime()
    }

    /**
     * Add the specified number of months to given date.
     *
     * @param date The date
     * @param number number of months to add
     * @return Date
     */
    @CompileDynamic
    static Date addMonths(Date date, int number) {
        Date result
        use(TimeCategory) {
            result = date + number.months
        }
        return result
    }


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


    /**
     * Checks if the current day number is equal to specified day number in a given period.
     *
     * @period ChronoUnit DAYS WEEKS or MONTHS
     * @dayNumber 1-30 for monthly, 1-7 for weekly (1 is Sunday)
     * @return is today the date for a specified period and dayInPeriod
     */
    static boolean isTodayTheDate(ChronoUnit period, int dayNumber) {
        LocalDate thedate = LocalDate.now()
        switch (period) {
            case ChronoUnit.DAYS:
                return true
            case ChronoUnit.WEEKS:
                return DayOfWeek.from(thedate).value == dayNumber
            case ChronoUnit.MONTHS:
                return thedate.getDayOfMonth() == dayNumber
            default:
                return false
        }
    }

    /**
     * Get Month difference between two dates
     *
     * Returns int
     * one.month = two.month : 0
     * one.month = (two.month - 1) : 1
     * one.month = (two.month + 1) : -1
     */
    static int getMonthDiff(Date one, Date two) {
        Calendar start = Calendar.getInstance()
        start.setTime(one)
        Calendar end = Calendar.getInstance()
        end.setTime(two)

        int diffYear = end.get(Calendar.YEAR) - start.get(Calendar.YEAR)
        int diffMonth = diffYear * 12 + end.get(Calendar.MONTH) - start.get(Calendar.MONTH)
        return diffMonth
    }

    /**
     * Returns the number of days between two dates.
     *
     *  start == end : 0
     *  start is 10 days after end : 10
     *  start is 10 days before end: -10
     *
     * @return int number of days
     */
    @CompileDynamic
    static int getDaysBetween(Date start, Date end) {
        Validate.notNull(start, "[Start date]")
        Validate.notNull(end, "[End date]")

        int days

        use(TimeCategory) {
            TimeDuration duration = (start - end)
            days = duration.days
        }

        return days
    }

    /*
    * Returns the last day of month for the specified date.
    *
    * @date date the last day of month should be returned
    * @addMonth to move the month for which is the last day is displayed, if 0 - then for the month of the date
    * @return a date which represents the last day of month
    */

    static Date getLastDayOfMonth(Date date, int addMonth = 0) {
        def locDate = LocalDateUtils.getLastDateOfMonth(date.toLocalDate())
        if(addMonth) locDate = locDate.plusMonths(addMonth as Long)
        return locDate.toDate()
    }

    static LocalDate getLastDayOfMonth(LocalDate date, int addMonth = 0) {
        def locDate = LocalDateUtils.getLastDateOfMonth(date)
        if(addMonth) locDate = locDate.plusMonths(addMonth as Long)
        return locDate
    }

    /*
    * Returns the first day of month for the specified date.
    *
    * @date date the first day of month should be returned
    * @addMonth to move the month for which is the first day is displayed, if 0 - then for the month of the date
    * @return a date which represents the first day of month
    */

    static Date getFirstDayOfMonth(Date date, int addMonth = 0) {
        def locDate = LocalDateUtils.getFirstDateOfMonth(date.toLocalDate())
        if(addMonth) locDate = locDate.plusMonths(addMonth as Long)
        return locDate.toDate()
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

    static boolean isSameDay(Date date1, Date date2){
        def ld1 = LocalDateTime.ofInstant(date1.toInstant(), ZoneId.systemDefault()).toLocalDate()
        def ld2 = LocalDateTime.ofInstant(date2.toInstant(), ZoneId.systemDefault()).toLocalDate()
        return ld1 == ld2
    }

}
