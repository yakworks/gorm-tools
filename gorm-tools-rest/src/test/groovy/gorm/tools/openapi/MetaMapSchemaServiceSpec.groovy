/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.openapi

import gorm.tools.beans.map.MetaMapIncludes
import gorm.tools.beans.map.MetaMapIncludesBuilder
import gorm.tools.testing.unit.DataRepoTest
import io.swagger.v3.oas.models.media.DateTimeSchema
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.NumberSchema
import io.swagger.v3.oas.models.media.StringSchema
import spock.lang.Specification
import yakworks.commons.map.MapFlattener
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgFlex

/**
 * sanity check test for service, which is just a wrapper so cacheable works
 */
class MetaMapSchemaServiceSpec extends Specification implements DataRepoTest {

    MetaMapSchemaService metaMapSchemaService = new MetaMapSchemaService()
    void setupSpec() {
        //mockDomain Person
        mockDomains Org, OrgFlex, Location
    }

    void "test buildIncludesMap simple"(){
        when:
        MetaMapIncludes mmIncs = MetaMapIncludesBuilder.build("Org", ['id', 'num', 'name'])
        MetaMapSchema mmSchema = metaMapSchemaService.getCachedMetaMapSchema(mmIncs)

        then:
        mmSchema
        //sanity check
        mmIncs.shortClassName == 'Org'
        mmIncs.props.keySet() == ['id', 'num', 'name'] as Set

        mmSchema.props.keySet() == ['id', 'num', 'name'] as Set

    }

}
