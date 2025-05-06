/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.config

import groovy.transform.CompileStatic

import org.springframework.boot.context.properties.ConfigurationProperties

import yakworks.rally.orgs.model.OrgType

@ConfigurationProperties(prefix="app.orgs", ignoreUnknownFields=false)
@CompileStatic
class OrgProps {

    /**
     * <p> The app can be used consolidate multiple accounting systems into a single central repository.
     * This is done by partitioning the data by organizations(Orgs) with a specific type (partitionOrgType),
     * where each Organization with that type owns or segments the different systems or sets of books.
     *
     * <p> The organization type or level (Company, Division, etc..) the data is segmented or "partioned" by.
     * The default is Company and there can be many companies under a single Client for example.
     * This is the Organization mapped to an ERP or GL or set of books. Can also be a considered a tenant.
     *
     * <p> Example 1, partition.orgType=Company: <br>
     *  - Google LLC and Waymo LLC are 2 Companies under the Alphabet Inc. ( which would be the Client). <br>
     *  - every transaction will be required to have either the Google or Waymo company assigned. <br>
     *  - Google is using an Oracle ERP and Waymo is using SAP. <br>
     *  - Google may have 2 Divisions and Waymo has 2, but those division are segments for reporting
     *    and not a seperate set of books or different ERPs. <br>
     *  - AP and AR can be applied across divisions but are restricted across companies. <br>
     *
     * Another common option is the Division type, where there can be many divisions rolling up into multiple companies,
     * but the GL is tracked at the Division level or each division represents a different accounting/ERP system.
     *
     * <p> Example 2, partition.orgType=Division: <br>
     *  - Similiar concepts as the Company partition but a lower level, Company is still used to group but the set of books for accounting
     *    is done at the lower division level. <br>
     *  - Just as above, Google LLC and Waymo LLC are 2 Companies under the Alphabet Inc. ( which would be the Client).<br>
     *  - The companies in this case dont neccesarily drive accounting and are the top level of the Dimension or Heirarchy.<br>
     *  - Google has 2 Divisions (Google Ads, and Google Apps) and Waymo has 2 (Waymo Tech and Waymo Cars).
     *    Each of those divisions is a separate set of books or a separate instance.<br>
     *  - Every customer and transaction will be required to have one of the divisions assigned. Google Ads, Google Apps, Waymo Tech or Waymo Cars. <br>
     *  - Google is using an Oracle ERP instances separated by what Oracle calls a "Company" and will be mapped to our Divisions <br>
     *  - Waymo has 2 SAP instances, one for Waymo Tech and one for Waymo Cars. <br>
     *  - AP and AR will be restricted to how its applied across Divisions. <br>
     *
     */
    PartitionConfig partition = new PartitionConfig()

    MemberConfig members = new MemberConfig()

    static class PartitionConfig {
        /**
         * A normal basic installation this will be false and there will be One single master Company (id=2) for a single set of books.
         * Set to true to activate multiple Org partitions, will then allow multiple Orgs with the specified orgType (Company by default)
         */
        boolean enabled = false

        OrgType type = OrgType.Company
    }

    static class MemberConfig {
        /** Set to true to activate members and dimension hierarchies */
        boolean enabled = true

        /**
         * The default dimension.
         * for example: [Customer, Company] is the default.
         */
        List<OrgType> dimension = [OrgType.Customer, OrgType.Company]

        void setDimension(List v){
            //Spring binding doesnt convert list strings to OrgTypes for some reason here, do it manually
            dimension = v.collect { it as OrgType}
        }

        List<OrgType> dimension2

        void setDimension2(List v){
            dimension2 = v.collect { it as OrgType}
        }
    }

}
