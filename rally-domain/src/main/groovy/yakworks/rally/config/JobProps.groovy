/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.config

import groovy.transform.CompileStatic

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Config prioperties for the syncJobs.
 * Can setup defaults and whats availiable.
 *
 */
@ConfigurationProperties(prefix="app.job")
@CompileStatic
class JobProps {

    List<String> maintenanceWindow
    List<String> maintenanceZone

    static class MaintenanceProps {
        List<String> crons
        String zone
    }
}
