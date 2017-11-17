package gorm.tools.beans

import groovy.time.TimeCategory
import groovy.time.TimeDuration
import groovy.transform.CompileStatic
import groovy.transform.CompileDynamic

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.regex.Pattern
import java.text.ParseException
import java.text.DateFormatSymbols
import org.apache.commons.lang.Validate

/**
 * Provides a set of methods for parsing/formatting and making custom manipulations with dates.
 * (e.g. to get a number of days between dates or to get last day of month, etc)
 */
@SuppressWarnings(['MethodCount', 'EmptyCatchBlock', 'ExplicitCallToGetAtMethod'])
@CompileStatic
class DateUtil {

    static final Pattern GMT_MILLIS = ~/\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z/
    static final Pattern TZ_LESS = ~/\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/
    static final Pattern GMT_SECONDS = ~/\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z/

    /**
     * Parse date sent by client (in a JSON).
     * Expected format: 2000-03-30T22:00:00.000Z or 2000-03-30T22:00:00Z
     *
     * @param date formatted date
     * @return parsed date
     * @throws ParseException if it cannot recognize a date format
     */
    static Date parseJsonDate(String date) {
        if (date == null) {
            return null
        }
        date = date.trim()
        if (date.length() == 0) {
            return null
        }
        DateFormat dateFormat = new SimpleDateFormat('yyyy-MM-dd')
        switch (date) {
            case GMT_MILLIS:
                date = date.replaceFirst('Z$', '-0000')
                dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                break
            case GMT_SECONDS:
                date = date.replaceFirst('Z$', '-0000')
                dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                break
            case TZ_LESS:
                dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                break
        }

        return dateFormat.parse(date)
    }

    /**
     * Returns a string representation of a given date in the 'yyyy-MM-dd'T'HH:mm:ss.SSSZ' format.
     *
     * @param date a date to convert into a string
     * @return a string representation of a given date
     */
    static String dateToJsonString(Date date) {
        dateToString(date, "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    }

    /**
     * Converts a string representation of date to Date.
     * Expected format: yyyy-MM-dd
     *
     * @param date a string with date in the format "yyyy-MM-dd"
     * @return a date instance
     */
    static Date stringToDate(String date) {
        convertStringToDateTime(date, "yyyy-MM-dd")
    }

    /**
     * Converts a Date into a string using a specified format.
     *
     * @param date   a date to covert
     * @param format a date format, by default "MM/dd/yyyy hh:mm:ss"
     * @return a string representation of a Date object or empty string
     */
    static String dateToString(Date date, String format = 'MM/dd/yyyy hh:mm:ss') {
        DateFormat df = new SimpleDateFormat(format)
        String dtStr = ''
        try {
            dtStr = df.format(date)
        } catch (ParseException e) {
            //e.printStackTrace()
        }
        return dtStr
    }

    /**
     * Returns the next month for current date and sets the day number to 1.
     *
     * @return the next month
     */
    static Date getNextMonth(){
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
    static Date getTwoMonthsBack(){
        return shiftCurrentDateByMonths(-2)
    }

    @Deprecated
    static Date getThreeMonthsBack(){
        return shiftCurrentDateByMonths(-3)
    }

    @Deprecated
    static Date getFourMonthsBack(){
        return shiftCurrentDateByMonths(-4)
    }

    /**
     * Shifts the current date by specified number of months
     * and sets current day of month to 1.
     *
     * @param months  number of months to shift
     * @return a date which shifted on specified number of months from now
     */
    static Date shiftCurrentDateByMonths(int months){
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
        return  result
    }

    /**
     * Returns the first day of the current month and sets time to midnight.
     */
    static Date getFirstDateOfMonth() {
        Calendar startDate = getCurrentCalendarInstance()
        startDate.set(Calendar.DATE, 1)
        return setTimeAsOfMidnight(startDate).getTime()
    }

    /**
     * Returns the first day of the current week and sets time to midnight.
     */
    static Date getFirstDayOfWeek() {
        Calendar startDate = getCurrentCalendarInstance()
        int dayOfWeek = startDate.get(Calendar.DAY_OF_WEEK)
        while (dayOfWeek  != Calendar.MONDAY) {
            startDate.add(Calendar.DATE, -1)
            dayOfWeek = startDate.get(Calendar.DAY_OF_WEEK)
        }
        return setTimeAsOfMidnight(startDate).getTime()
    }

    /**
     * Returns the last day of the current week and sets time to before midnight (23:59:59).
     */
    static Date getLastDayOfWeek() {
        Calendar endDate = getCurrentCalendarInstance()
        int dayOfWeek = endDate.get(Calendar.DAY_OF_WEEK)
        while(dayOfWeek  != Calendar.SUNDAY) {
            endDate.add(Calendar.DATE, 1)
            dayOfWeek = endDate.get(Calendar.DAY_OF_WEEK)
        }
        return setTimeBeforeMidnight(endDate).getTime()
    }

    //looks like it's not used
    @Deprecated
    static Date getLastWeekEndDate() {
        Calendar endDate = getCurrentCalendarInstance()
        int dayOfWeek = endDate.get(Calendar.DAY_OF_WEEK)
        while(dayOfWeek  != Calendar.SUNDAY) {
            endDate.add(Calendar.DATE, -1)
            dayOfWeek = endDate.get(Calendar.DAY_OF_WEEK)
        }
        return setTimeBeforeMidnight(endDate).getTime()
    }

    //looks like it's not used
    @Deprecated
    static int getLastWeekStartDate() {
        Calendar startDate = getCurrentCalendarInstance()
        int dayOfWeek = startDate.get(Calendar.DAY_OF_WEEK)
        while(dayOfWeek != Calendar.SUNDAY) {
            startDate.add(Calendar.DATE, -1)
            dayOfWeek = startDate.get(Calendar.DAY_OF_WEEK)
        }
        while(dayOfWeek != Calendar.MONDAY) {
            startDate.add(Calendar.DATE, -1)
            dayOfWeek = startDate.get(Calendar.DAY_OF_WEEK)
        }
        return dayOfWeek
    }

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
    static String getMonthLetter(String periodToPost){
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
    static String getMonthLetterByNum(int monthNumber) {
        String[] monthNames = new DateFormatSymbols().getMonths()
        monthNumber in (1..12) ? monthNames[monthNumber-1].getAt(0) : '?'
    }

    //didn't find any usage
    @Deprecated
    static Date stringToDateTime(String strDt) {
        convertStringToDateTime(strDt, "yyyy-MM-dd'T'HH:mm:ss")
    }

    /**
     * Converts a date to a specified format.
     *
     * @param strDt a date as a string
     * @param format a format
     * @return a date
     */
    static Date convertStringToDateTime(String strDt, String format) {
        DateFormat df = new SimpleDateFormat(format)
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
     * Sets time to midnight(00:00:00) for a given date instance.
     *
     * @param date a date for which to setup time
     * @return a date with time set to midnight
     */
    static Date setToMidnight(Date date) {
        Calendar cal = Calendar.getInstance()
        cal.setTime(date)
        setTimeAsOfMidnight(cal)
        return cal.getTime()
    }

    /**
     * Sets time to midnight(00:00:00) for a given date instance.
     *
     * @param date a date for which to setup time
     * @return a date with time set to midnight
     */
    static Calendar setTimeAsOfMidnight(Calendar cal) {
        return setTime(cal, 0, 0, 0)
    }

    /**
     * Sets time to 23:59:59 for a given calendar instance.
     *
     * @param date a calendar for which to setup time
     * @return a calendar with time set to 23:59:59
     */
    static Calendar setTimeBeforeMidnight(Calendar cal){
        return setTime(cal, 23, 59, 59)
    }

    /**
     * Sets time for a given calendar instance.
     *
     * @param cal          a calendar instance
     * @param hours        hours, by default is 0
     * @param minutes      minutes, by default is 0
     * @param seconds      seconds, by default is 0
     * @param milliseconds milliseconds, by default is 0
     * @return
     */
    static Calendar setTime(Calendar cal, int hours = 0, int minutes = 0,
                            int seconds = 0, int milliseconds = 0) {
        cal.set(Calendar.HOUR_OF_DAY, hours)
        cal.set(Calendar.MINUTE, minutes)
        cal.set(Calendar.SECOND, seconds)
        cal.set(Calendar.MILLISECOND, milliseconds)
        return cal
    }

    /**
     * Checks if the current day number is equal to specified day number in a given period.
     *
     * @period     daily, weekly or monthly
     * @dayNumber  1-30 for monthly, 1-7 for weekly (1 is Sunday)
     * @return is today the date for a specified period and dayInPeriod
     */
    static boolean isTodayTheDate(String period, int dayNumber) {
        int dayOfMonth = new Date().getAt(Calendar.DAY_OF_MONTH)
        int dayOfWeek = new Date().getAt(Calendar.DAY_OF_WEEK)
        switch ( period.toLowerCase() ) {
            case "daily":
                return true
            case "weekly":
                return dayNumber == dayOfWeek
            case "monthly":
                return dayNumber == dayOfMonth
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
     *
     * @param one
     * @param two
     * @return
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
     *
     * @return int number of days
     */
    @CompileDynamic
    static int getDaysBetween(Date start, Date end) {
        Validate.notNull(start, "Start date is null")
        Validate.notNull(end, "End date is null")

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
        Calendar c = Calendar.getInstance()
        c.setTime(date)
        c.add(Calendar.MONTH, addMonth)
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
        c.getTime().clearTime()
    }

    /*
    * Returns the first day of month for the specified date.
    *
    * @date date the first day of month should be returned
    * @addMonth to move the month for which is the first day is displayed, if 0 - then for the month of the date
    * @return a date which represents the first day of month
    */
    static Date getFirstDayOfMonth(Date date, int addMonth = 0) {
        Calendar c = Calendar.getInstance()
        c.setTime(date)
        c.add(Calendar.MONTH, addMonth)
        c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH))
        c.getTime().clearTime()
    }
}
