/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.lang

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.function.Supplier
import java.util.regex.Pattern

import groovy.transform.CompileStatic

/**
 * Provides a set of methods for parsing/formatting ISO 8601 dates.
 * https://en.wikipedia.org/wiki/ISO_8601
 *
 */
@CompileStatic
class IsoDateUtil {
    //yyyy-MM-dd "2017-12-27"
    static final Pattern LOCAL_DATE = ~/\d{4}-\d{2}-\d{2}$/
    //yyyy-MM-dd'T'HH:mm:ss.SSSZ
    static final Pattern GMT_MILLIS = ~/\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z/
    //yyyy-MM-dd'T'HH:mm:ssZ
    static final Pattern GMT_SECONDS = ~/\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z/
    //yyyy-MM-dd'T'HH:mm:ss
    static final Pattern TZ_LESS = ~/\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/
    //yyyy-MM-dd'T'HH:mm
    static final Pattern TZ_LESS_HH_MM = ~/\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/

    //see https://stackoverflow.com/questions/4032967/json-date-to-java-date#4033027
    static final ThreadLocal<SimpleDateFormat> LOCAL_DATE_FORMAT = ThreadLocal.withInitial({
        SimpleDateFormat fmatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
        https://stackoverflow.com/questions/2891361/how-to-set-time-zone-of-a-java-util-date
        fmatter.setTimeZone(TimeZone.getTimeZone('UTC'))
        return fmatter
    } as Supplier<SimpleDateFormat>)

    static final ThreadLocal<SimpleDateFormat> DATE_TIME_FORMAT = ThreadLocal.withInitial({
        SimpleDateFormat fmatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        fmatter.setTimeZone(TimeZone.getTimeZone('UTC'))
        return fmatter
    } as Supplier<SimpleDateFormat>)

    /**
     * Parse date sent by client (mostly JSON).
     * Expected formats: 2000-03-30, 2000-03-30T22:11:22.123Z , 2000-03-30T22:00:00Z or yyyy-MM-dd'T'HH:mm:ss
     * Assumes all timeZones are UTC
     *
     * //see https://stackoverflow.com/questions/10286204/the-right-json-date-format
     *
     * @param date formatted date
     * @return parsed date
     * @throws java.text.ParseException if it cannot recognize a date format
     */
    static Date parse(String date) {
        date = date?.trim()
        if (!date) return null

        //default for GMT_MILLIS match
        DateFormat dateFormat = DATE_TIME_FORMAT.get()

        //if-then is slightly faster than a switch here
        if (date.matches(GMT_MILLIS)) {
            return dateFormat.parse(date)
        } else if (date.matches(LOCAL_DATE)) {
            dateFormat = LOCAL_DATE_FORMAT.get()
        } else if (date.matches(GMT_SECONDS)) {
            date = date.replaceFirst('Z$', '.000Z')
        } else if (date.matches(TZ_LESS)) {
            date = "${date}.000Z"
        } else if (date.matches(TZ_LESS_HH_MM)) {
            date = "${date}:00.000Z"
        }

        return dateFormat.parse(date)
    }

    static LocalDate parseLocalDate(String date) {
        LocalDateUtils.parse(date)
    }

    static LocalDateTime parseLocalDateTime(String date) {
        LocalDateUtils.parseLocalDateTime(date)
    }

    /**
     * Returns a string representation of a given date in the 'yyyy-MM-dd'T'HH:mm:ss.SSSZ' format. UTC ISO standard
     *
     * @param date a date to convert into a string
     * @return a string representation of a given date
     */
    static String format(Date date) {
        return DATE_TIME_FORMAT.get().format(date)
    }

    static String format(LocalDateTime date) {
        DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(date)
    }

    static String format(LocalDate date, String format = null) {
        DateTimeFormatter formatter = format ? DateTimeFormatter.ofPattern(format) : DateTimeFormatter.ISO_LOCAL_DATE
        return date.format(formatter)
    }

    /**
     * formats to string if isDate(value) is true, should make that check first as if it evaulates to false then returns null
     */
    static String format(Object value) {
        if(!isDate(value)) return null
        if(value instanceof Date) return format((Date)value)
        if(value instanceof LocalDate) return format((LocalDate)value)
        if(value instanceof LocalDateTime) return format((LocalDateTime)value)
    }

    static isDate(Object value){
        return value instanceof Date || value instanceof LocalDate || value instanceof LocalDateTime
    }

    /**
     * Converts a Date into a string using a specified format.
     *
     * @param date a date to covert
     * @param format a date format, by default "MM/dd/yyyy hh:mm:ss"
     * @return a string representation of a Date object or empty string
     */
    @SuppressWarnings(['EmptyCatchBlock'])
    static String dateToString(Date date, String format = 'MM/dd/yyyy hh:mm:ss') {
        DateFormat df = new SimpleDateFormat(format, Locale.US)
        String dtStr = ''
        try {
            dtStr = df.format(date)
        } catch (ParseException e) {
            //e.printStackTrace()
        }
        return dtStr
    }

}
