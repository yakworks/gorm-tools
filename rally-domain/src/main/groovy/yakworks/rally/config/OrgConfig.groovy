/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.config

import groovy.transform.CompileStatic

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

import yakworks.rally.orgs.model.OrgType

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix="app.orgs")
@CompileStatic
class OrgConfig {

    /**
     * The organization type or level (Company, Division, etc..) the data is segmented or "partioned" by.
     * The default is Company and there can be many companies under a single Client for example.
     * This is the Organization mapped to an ERP or GL or set of books. Can also be a considered a tenant.
     *
     * Example 1, OrgType=Company:
     *  - Google LLC and Waymo LLC are 2 Companies under the Alphabet Inc. ( which would be the Client).
     *  - every transaction will be required to have either the Google or Waymo company assigned.
     *  - Google is using an Oracle ERP and Waymo is using SAP.
     *  - Google may have 2 Divisions and Waymo has 2, but those division are segments for reporting
     *    and not a seperate set of books or different ERPs.
     *  - AP and AR can be applied across divisions but are restricted across comapnies.
     *
     * Another common option is the Division type, where there can be many divisions rolling up into multiple companies,
     * but the GL is tracked at the Division level or each division represents a different accounting/ERP system.
     *
     * Example 2, partitionOrgType=Division:
     *  - Essentially the same as the Company partion but a lower level, Company is still used to group but the set of books for accounting
     *    is done at the lower division level.
     *  - Just as above, Google LLC and Waymo LLC are 2 Companies under the Alphabet Inc. ( which would be the Client).
     *  - The companies in this case dont neccesarily drive accounting and are the top level of the Dimension or Heirarchy.
     *  - Google has 2 Divisions (Google Ads, and Google Apps) and Waymo has 2 (Waymo 1 and 2).
     *    Each of those divisions is a seperate set of books or a seperate instance.
     *  - Every transaction will be required to have one of the divisions assigned. Google Ads, Google Apps, Waymo1 or Waymo2.
     *  - Google is using an Oracle ERP instances with sepeartate by what Oracle calls a "Company" and will be mapped to our Divisions
     *  - Waymo has 2 SAP instances, one for each of the 2 Divisions.
     *  - AP and AR will be restricted to how its applied across Divisions.
     *
     */
    OrgType partitionOrgType = OrgType.Company

    /**
     * Whether there will be multiple Organizations for whats set in partitionOrgType.
     * For example, partitionOrgType will default to Company. In a straight forward instance of the App this will be false and there will
     * be a single Company for this app (Company id 2 by default) and a single set of books.
     * When this is set to true then multiple Companies (or Divisions) will be allowed.
     */
    boolean multiPartition = false

    List<String> dimensions

}
