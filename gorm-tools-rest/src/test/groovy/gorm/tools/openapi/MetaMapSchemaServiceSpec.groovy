/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.openapi

import gorm.tools.metamap.MetaMapEntityService
import gorm.tools.testing.unit.DataRepoTest
import spock.lang.Shared
import spock.lang.Specification
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgFlex

/**
 * sanity check test for service, which is just a wrapper so cacheable works
 */
class MetaMapSchemaServiceSpec extends Specification implements DataRepoTest {

    @Shared
    def metaMapSchemaService = new MetaMapSchemaService()
    // def metaMapEntityService = new MetaMapEntityService()

    void setupSpec() {
        //mockDomain Person
        mockDomains Org, OrgFlex, Location
        metaMapSchemaService.metaMapEntityService = new MetaMapEntityService()
    }

    void "test buildIncludesMap simple"(){
        when:
        // MetaMapIncludes mmIncs = MetaMapIncludesBuilder.build("Org", ['id', 'num', 'name'])
        MetaMapSchema mmSchema = metaMapSchemaService.getSchema("yakworks.rally.orgs.model.Org", ['id', 'num', 'name'])

        then:
        mmSchema
        //sanity check
        mmSchema.className == 'yakworks.rally.orgs.model.Org'
        mmSchema.props.keySet() == ['id', 'num', 'name'] as Set

    }

}
