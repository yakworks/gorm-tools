/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally

import grails.plugins.Plugin

@SuppressWarnings('Indentation')
class RallyDomainGrailsPlugin extends Plugin {

    def loadAfter = ['boot-security']

    Closure doWithSpring() { {->
        //FIXME not sure this is needed, hack to get rest app working but not sure why its not picked up
        // as its already defined in boot-security
        // def securityConf = SpringSecurityUtils.securityConfig
        // if (securityConf.active) {
        //     userOrgService(UserOrgService)
        // }

        //temp in place to assign defualt orgId to user as Company default (2)
        // rallyEventListener(RallyEventListener) { bean ->
        //     bean.lazyInit = true
        //     bean.autowire = true
        // }

        // orgCopier(OrgCopier, lazy())
        // orgDimensionService(OrgDimensionService, lazy())
        // orgMemberService(OrgMemberService, lazy())
    }}

}
