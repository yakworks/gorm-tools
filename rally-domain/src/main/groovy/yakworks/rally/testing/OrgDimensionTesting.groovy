/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.testing

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import yakworks.rally.config.OrgProps
import yakworks.rally.orgs.OrgDimensionService
import yakworks.rally.orgs.model.OrgType
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
        //make sure its enabled
        orgProps.members.enabled = true
        //List<OrgType> arr = dimension.collect{ it as  OrgType }
        orgProps.members.dimension = dimension
        orgProps.members.dimension2 = dimension2 ?: []
        reinit()
    }

    /** Used for testing to null out and clear the dimensions */
    static void emptyDimensions(){
        orgProps.members.with {
            dimension = []
            dimension2 = []
        }
        reinit()
    }

    /** Resets the dimensions to the default */
    static void resetDimensions(){
        orgProps.members.with {
            dimension = [OrgType.Customer, OrgType.Company]
            dimension2 = []
        }
        reinit()
    }

    @CompileDynamic
    static void reinit(){
        orgDimensionService.isInitialized = false
        orgDimensionService.init()
    }

    //Legacy , Parse dot path like Customer.Branch.Division into array and init.
    static void setDimensionsPath(String dim1, String dim2 =  null) {
        List<OrgType> dim1List = dim1.tokenize('.').collect{ it as  OrgType }
        List<OrgType> dim2List
        if(dim2) dim2List = dim2.tokenize('.').collect{ it as  OrgType }
        setDimensions(dim1List, dim2List)
    }
}
