/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.config

import groovy.transform.CompileStatic

import org.springframework.boot.context.properties.ConfigurationProperties

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
     * Should use the region. so for CST set the "America/Chicago" and EST "America/New_York", etc..
     */
    String zone
}
