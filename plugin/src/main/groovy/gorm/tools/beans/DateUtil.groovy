package gorm.tools.beans

import groovy.time.TimeCategory
import groovy.time.TimeDuration
import groovy.transform.CompileStatic
import groovy.transform.CompileDynamic

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.regex.Pattern
import java.text.ParseException
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
     * Returns a date in the format 'yyyy-MM-dd'T'HH:mm:ss.SSSZ'.
     */
    static String dateToJsonString(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        return dateFormat.format(date)
    }

    static Date stringToDate(String strDt){
        Date date = convertStringToDateTime(strDt, "yyyy-MM-dd")
        return date
    }

    static String dateToString(Date date){
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss")
        String dtStr = ''
        try {
            dtStr = df.format(date)
        } catch (ParseException e) {
            //e.printStackTrace()
        }
        return dtStr
    }

    static String dateToString(Date date, String format){
        DateFormat df = new SimpleDateFormat(format)
        String dtStr = ''
        try {
            dtStr = df.format(date)
        } catch (ParseException e) {
            //e.printStackTrace()
        }
        return dtStr
    }

    static Date getNextMonth(){
        return shiftCurrentDateByMonths(1)
    }

    static Date getPreviousMonth(){
        return shiftCurrentDateByMonths(-1)
    }

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
     * Add specified number of months to given date.
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
    static Date getFirstDayOfWeek(){
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
    static Date getLastDayOfWeek(){
        Calendar endDate = getCurrentCalendarInstance()
        int dayOfWeek = endDate.get(Calendar.DAY_OF_WEEK)
        while(dayOfWeek  != Calendar.SUNDAY) {
            endDate.add(Calendar.DATE, 1)
            dayOfWeek = endDate.get(Calendar.DAY_OF_WEEK)
        }
        return setTimeBeforeMidnight(endDate).getTime()
    }

    static Date getLastWeekEndDate(){
        Calendar endDate = getCurrentCalendarInstance()
        int dayOfWeek = endDate.get(Calendar.DAY_OF_WEEK)
        while(dayOfWeek  != Calendar.SUNDAY) {
            endDate.add(Calendar.DATE, -1)
            dayOfWeek = endDate.get(Calendar.DAY_OF_WEEK)
        }
        return setTimeBeforeMidnight(endDate).getTime()
    }

    static int getLastWeekStartDate(){
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

    static Calendar getCurrentCalendarInstance(){
        Calendar currentDate = Calendar.getInstance()
        currentDate.setTime(new Date())
        return currentDate
    }

    static Calendar getCalendarInstance_ByDate(Date date){
        Calendar calendarDate = Calendar.getInstance()
        calendarDate.setTime(date)
        return calendarDate
    }

    static String getMonthLetter(String periodToPost){
        String twoNumbers=""
        twoNumbers=periodToPost?.substring(4, 6)
        String monthLetter=twoNumbers
        switch(twoNumbers){
            case '01': monthLetter = 'J'
                break
            case '02': monthLetter = 'F'
                break
            case '03': monthLetter = 'M'
                break
            case '04': monthLetter = 'A'
                break
            case '05': monthLetter = 'M'
                break
            case '06': monthLetter = 'J'
                break
            case '07': monthLetter = 'J'
                break
            case '08': monthLetter = 'A'
                break
            case '09': monthLetter = 'S'
                break
            case '10': monthLetter = 'O'
                break
            case '11': monthLetter = 'N'
                break
            case '12': monthLetter = 'D'
                break
            default: monthLetter = '?'
        }
        return monthLetter
    }

    static Date stringToDateTime(String strDt) {
        Date date = convertStringToDateTime(strDt, "yyyy-MM-dd'T'HH:mm:ss")
        return date
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
    static int getYearOf (Date date) {
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
        Calendar lastModifiedDateCalendar = getCalendarInstance_ByDate(date)
        long diff = getCurrentCalendarInstance().getTimeInMillis() - lastModifiedDateCalendar.getTimeInMillis()
        return Math.abs(diff) / (60 * 60 * 1000) as long
    }

    static Date setToMidnight(Date date) {
        Calendar cal = Calendar.getInstance()
        cal.setTime(date)
        setTimeAsOfMidnight(cal)
        return cal.getTime()
    }

    static Calendar setTimeAsOfMidnight(Calendar cal) {
        return setTime(cal, 0, 0, 0)
    }

    static Calendar setTimeBeforeMidnight(Calendar cal){
        return setTime(cal, 23, 59, 59)
    }

    static Calendar setTime(Calendar cal, int hours = 0, int minutes = 0,
                            int seconds = 0, int milliseconds = 0) {
        cal.set(Calendar.HOUR_OF_DAY, hours)
        cal.set(Calendar.MINUTE, minutes)
        cal.set(Calendar.SECOND, seconds)
        cal.set(Calendar.MILLISECOND, milliseconds)
        return cal
    }

    /**
     * Checks if the current day number is equal to specified day number.
     * It is possible to specify day number of a month or week.
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
