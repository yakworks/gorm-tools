/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally

import grails.plugin.springsecurity.SpringSecurityUtils
import yakworks.rally.orgs.OrgCopier
import yakworks.rally.orgs.OrgDimensionService
import yakworks.rally.orgs.OrgMemberService
import yakworks.rally.orgs.UserOrgService

@SuppressWarnings('Indentation')
class RallyDomainGrailsPlugin extends grails.plugins.Plugin {

    def loadAfter = ['gorm-tools-security']

    Closure doWithSpring() { {->
        //FIXME not sure this is needed, hack to get rest app working but not sure why its not picked up
        // as its already defined in gorm-tools-security
        def securityConf = SpringSecurityUtils.securityConfig
        if (securityConf.active) {
            userOrgService(UserOrgService)
        }

        // orgCopier(OrgCopier, lazy())
        // orgDimensionService(OrgDimensionService, lazy())
        // orgMemberService(OrgMemberService, lazy())
    }}
}
