/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.testing

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import yakworks.rally.config.OrgProps
import yakworks.rally.orgs.OrgDimensionService
import yakworks.spring.AppCtx

/**
 * See docs/design-principles/org-members-dimensions.md
 * Organization types are arranged in hierarchie, and configured in AppSetupConfig.
 *
 * OrgDimensionService provides methods to parse the orgdimension level hierarchie and get children, parents and
 * immediate parent orgtypes for any orgtype.
 */
@CompileStatic
class OrgDimensionTesting {

    static OrgDimensionService getOrgDimensionService(){
        AppCtx.get(OrgDimensionService)
    }

    static OrgProps getOrgProps(){
        AppCtx.get(OrgProps)
    }

    // /** The list of dimensions paths that are valid */
    static void setDimensions(List dimension, List dimension2 = null){
        orgProps.members.enabled = true
        //List<OrgType> arr = dimension.collect{ it as  OrgType }
        orgProps.members.dimension = dimension
        if(dimension2) orgProps.members.dimension2 = dimension2
        reinit()
    }

    /** Used for testing to null out and clear the dimensions */
    static void clearDimensions(){
        orgProps.members.with {
            dimension = []
            dimension2 = []
        }
        reinit()
    }

    @CompileDynamic
    static void reinit(){
        orgDimensionService.isInitialized = false
        orgDimensionService.init()
    }

}
