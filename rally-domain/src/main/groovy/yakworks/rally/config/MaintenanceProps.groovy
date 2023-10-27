/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.config

import groovy.transform.CompileStatic

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * properties/config for when app will be be in Maintenance mode.
 * Work with a list of crons expressions and an have a time zone.
 * Used in the jobs for now but can be used in other areas of the app to make sure sensitive areas are not touched while in maint mode.
 */
@ConfigurationProperties(prefix="app.maintenance")
@CompileStatic
class MaintenanceProps {
    /**
     * maintenance window crons.
     * Will be considered inside of the maintenance window if the current time falls within any of the crons in the list
     * The sconds and minutes part should alway be star *
     * Examples:
     *  - '* * 2,3 * * TUE-SAT'
     *  - '* * 20,21,22,23 * * SAT,SUN'
     *
     * the above says to be in  maint if current time is within hour 2 and 3 on Tue-SAT or within hours 20-23 on SAT and SUN
     */
    List<String> crons

    /**
     * Timezone for the crons expressions. UTC is default.
     * Should use the region not 3 letters acronym,
     * "America/Chicago"=CST, "America/New_York"=EST, "America/Denver"=MST, "America/Los_Angeles"=PST
     */
    String zone
}
