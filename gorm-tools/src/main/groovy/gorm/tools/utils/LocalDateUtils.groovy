/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.utils


import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

import groovy.transform.CompileStatic

/**
 * custom manipulations with dates.
 * (e.g. to get a number of days between dates or to get last day of month, etc)
 */
@CompileStatic
@SuppressWarnings(['MethodCount'])
class LocalDateUtils {

    /**
     * Returns the first day of the current month and sets time to midnight.
     */
    static LocalDate getFirstDateOfMonth() {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * Returns the last day of the current week and sets time to before midnight (23:59:59).
     */
    static LocalDate getLastDayOfWeek() {
        LocalDate.now().with(DayOfWeek.SUNDAY)
    }

}
