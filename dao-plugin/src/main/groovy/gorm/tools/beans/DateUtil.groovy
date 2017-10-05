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

@CompileStatic
class DateUtil {

    static Pattern GMT_MILLIS = ~/\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z/
    static Pattern TZ_LESS = ~/\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/
    static Pattern GMT_SECONDS = ~/\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z/

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

    static String dateToJsonString(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        return dateFormat.format(date)
    }

    public static Date stringToDate(String strDt){
        Date date = convertStringToDateTime(strDt,"yyyy-MM-dd")
        return date
    }

    public static String dateToString(Date date){
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss")
        String dtStr = ''
        try {
            dtStr = df.format(date)
        } catch (ParseException e) {
            e.printStackTrace()
        }
        return dtStr
    }

    public static String dateToString(Date date, String format){
        DateFormat df = new SimpleDateFormat(format)
        String dtStr = ''
        try {
            dtStr = df.format(date)
        } catch (ParseException e) {
            e.printStackTrace()
        }
        return dtStr
    }

    static Date getPreviousMonth(){
        Calendar previousMonth = getCurrentCalendarInstance()
        previousMonth.set(Calendar.DATE, 1)
        previousMonth.add(Calendar.MONTH, -1)
        return previousMonth.getTime()
    }

    static Date getTwoMonthsBack(){
        Calendar previousMonth = getCurrentCalendarInstance()
        previousMonth.set(Calendar.DATE, 1)
        previousMonth.add(Calendar.MONTH, -2)
        return previousMonth.getTime()
    }

    static Date getThreeMonthsBack(){
        Calendar previousMonth = getCurrentCalendarInstance()
        previousMonth.set(Calendar.DATE, 1)
        previousMonth.add(Calendar.MONTH, -3)
        return previousMonth.getTime()
    }

    static Date getFourMonthsBack(){
        Calendar previousMonth = getCurrentCalendarInstance()
        previousMonth.set(Calendar.DATE, 1)
        previousMonth.add(Calendar.MONTH, -4)
        return previousMonth.getTime()
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

    static Date getNextMonth(){
        Calendar nextMonth = getCurrentCalendarInstance()
        nextMonth.set(Calendar.DATE, 1)
        nextMonth.add(Calendar.MONTH, 1)
        return nextMonth.getTime()
    }

    static Date getFirstDateOfMonth(){
        Calendar startDate = getCurrentCalendarInstance()
        startDate.set(Calendar.DATE, 1)
        return setTimeAsOfMidnight(startDate).getTime()
    }

    static Date getFirstDayOfWeek(){
        Calendar startDate = getCurrentCalendarInstance()
        int dayOfWeek = startDate.get(Calendar.DAY_OF_WEEK)
        while (dayOfWeek  != Calendar.MONDAY) {
            startDate.add(Calendar.DATE, -1)
            dayOfWeek = startDate.get(Calendar.DAY_OF_WEEK)
        }
        return setTimeAsOfMidnight(startDate).getTime()
    }

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

    def static getLastWeekStartDate(){
        Calendar startDate = getCurrentCalendarInstance()
        int dayOfWeek = startDate.get(Calendar.DAY_OF_WEEK)
        while(dayOfWeek  != Calendar.SUNDAY) {
            startDate.add(Calendar.DATE, -1)
            dayOfWeek = startDate.get(Calendar.DAY_OF_WEEK)
        }
        while(dayOfWeek != Calendar.MONDAY) {
            startDate.add(Calendar.DATE, -1)
            dayOfWeek = startDate.get(Calendar.DAY_OF_WEEK)
        }
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
        twoNumbers=periodToPost?.substring(4,6)
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

    public static Date stringToDateTime(String strDt){
        Date date = convertStringToDateTime(strDt,"yyyy-MM-dd'T'HH:mm:ss")
        return date
    }

    static Date convertStringToDateTime(String strDt, String format){
        DateFormat df = new SimpleDateFormat(format)
        Date dtTmp = null
        try {
            dtTmp = df.parse(strDt)
        } catch (ParseException e) {
            e.printStackTrace()
        }
        return dtTmp
    }

    /**
     * Returns the year of the date specified.
     */
    public static int getYearOf (Date date) {
        GregorianCalendar gc = new GregorianCalendar()
        gc.setTime(date)
        return gc.get(GregorianCalendar.YEAR)
    }

    public static long getDateDifference_inHours(Date date){
        Calendar lastModifiedDateCalendar = getCalendarInstance_ByDate(date)
        long diff = getCurrentCalendarInstance().getTimeInMillis() - lastModifiedDateCalendar.getTimeInMillis()
        return diff / (60 * 60 * 1000) as long
    }

    public static Date setToMidnight(Date date) {
        Calendar cal = Calendar.getInstance()
        cal.setTime(date)
        setTimeAsOfMidnight(cal)
        return cal.getTime()
    }

    public static Calendar setTimeAsOfMidnight(Calendar cal){
        cal.set(Calendar.HOUR_OF_DAY, 0)            // set hour to midnight
        cal.set(Calendar.MINUTE, 0)                 // set minute in hour
        cal.set(Calendar.SECOND, 0)                 // set second in minute
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }

    public static Calendar setTimeBeforeMidnight(Calendar cal){
        cal.set(Calendar.HOUR_OF_DAY, 23)            // set hour to midnight
        cal.set(Calendar.MINUTE, 59)                 // set minute in hour
        cal.set(Calendar.SECOND, 59)                 // set second in minute
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }

    public static Date getLastDayOfMonth(Date orig) {
        Calendar calendar = Calendar.instance
        calendar.setTime(orig)
        int year = calendar.get(Calendar.YEAR)
        int month = calendar.get(Calendar.MONTH) + 1
        int day = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        SimpleDateFormat format = new SimpleDateFormat('yyyy-MM-dd')
        return format.parse("${year}-${month}-${day}".toString())
    }

    /**
     * @period daily, weekly or monthly
     * @dayInPeriod  1-30 for monthly, 1-7 for weekly (1 is Sunday)
     * @return is today the date for a specified period and dayInPeriod
     */
    public static boolean isTodayTheDate(String period, int dayInPeriod) {
        int dayOfMonth = new Date().getAt(Calendar.DAY_OF_MONTH)
        int dayOfWeek = new Date().getAt(Calendar.DAY_OF_WEEK)
        switch ( period.toLowerCase() ) {
            case "daily":
                return true
            case "weekly":
                return dayInPeriod == dayOfWeek
            case "monthly":
                return dayInPeriod == dayOfMonth
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
    public static int getMonthDiff(Date one, Date two) {
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
    public static int getDaysBetween(Date start, Date end) {
        Validate.notNull(start, "Start date is null")
        Validate.notNull(end, "End date is null")

        int days

        use(groovy.time.TimeCategory) {
            TimeDuration duration = (start - end)
            days = duration.days
        }

        return days
    }

    /*
    * Returns last day of month
    * @date date the last da of month should be returnd
    * @addMonth to move the month for wich is last day is displayed, if 0 - then for the month of the date
    */
    static Date getLastDayOfMonth(Date date, int addMonth) {
        Calendar c = Calendar.getInstance()
        c.setTime(date)
        c.add(Calendar.MONTH, addMonth)
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
        c.getTime().clearTime()
    }

    static Date getFirstDayOfMonth(Date date, int addMonth = 0) {
        Calendar c = Calendar.getInstance()
        c.setTime(date)
        c.add(Calendar.MONTH, addMonth)
        c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH))
        c.getTime().clearTime()
    }
}
