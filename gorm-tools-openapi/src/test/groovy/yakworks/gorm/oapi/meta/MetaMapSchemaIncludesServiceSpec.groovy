/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.oapi.meta

import gorm.tools.metamap.MetaMapIncludesBuilder
import gorm.tools.testing.unit.DataRepoTest
import io.swagger.v3.oas.models.media.DateTimeSchema
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.NumberSchema
import io.swagger.v3.oas.models.media.StringSchema
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import yakworks.meta.MetaMapIncludes
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgFlex

/**
 * sanity check test for service, which is just a wrapper so cacheable works
 */
class MetaMapSchemaIncludesServiceSpec extends Specification implements DataRepoTest {

    @Shared
    def metaMapSchemaIncludesService = new MetaMapSchemaIncludesService()

    void setupSpec() {
        //mockDomain Person
        mockDomains Org, OrgFlex, Location
    }

    void "test buildIncludesMap simple"(){
        when:
        // MetaMapIncludes mmIncs = MetaMapIncludesBuilder.build("Org", ['id', 'num', 'name'])
        MetaMapIncludes mmi = metaMapSchemaIncludesService.getMetaMapIncludes(Org.name, ['id', 'num', 'name'], [])

        then:
        mmi
        //sanity check
        mmi.className == 'yakworks.rally.orgs.model.Org'
        mmi.shortClassName == 'Org'
        mmi.schema
        mmi.propsMap.keySet() == ['id', 'num', 'name'] as Set

        mmi.propsMap.id.schema
        mmi.propsMap.num.schema
        mmi.propsMap.name.schema

        IntegerSchema idSchema = mmi.propsMap['id'].schema
        idSchema.type == 'integer'
        idSchema.format == 'int64'
        idSchema.readOnly

        StringSchema numSchema = mmi.propsMap['num'].schema
        numSchema.type == 'string'
        numSchema.maxLength == 50

        StringSchema nameSchema = mmi.propsMap['name'].schema
        nameSchema.type == 'string'
        nameSchema.maxLength == 100
    }

    void "test buildIncludesMap associations"(){
        when:
        MetaMapIncludes mmi = metaMapSchemaIncludesService.getMetaMapIncludes(Org.name, ['id', 'name', 'flex.date1', 'flex.text1', 'flex.num1'], [])

        then:
        mmi
        //sanity check
        mmi.shortClassName == 'Org'
        mmi.propsMap.keySet() == ['id', 'name', 'flex'] as Set

        IntegerSchema idSchema = mmi.propsMap['id'].schema
        idSchema.type == 'integer'
        idSchema.format == 'int64'
        idSchema.readOnly

        StringSchema nameSchema = mmi.propsMap['name'].schema
        nameSchema.type == 'string'
        nameSchema.maxLength == 100

        def flexPropsMap = mmi.propsMap['flex'].propsMap
        NumberSchema num1Schema = flexPropsMap['num1'].schema
        num1Schema.type == 'number'

        StringSchema text1Schema = flexPropsMap['text1'].schema
        text1Schema.type == 'string'

        DateTimeSchema date1Schema = flexPropsMap['date1'].schema
        date1Schema.type == 'string'
        date1Schema.format == "date-time"
    }

    @Ignore //FIXME
    void "MetaMapSchema flatten method"(){
        when:
        MetaMapIncludes mmIncs = MetaMapIncludesBuilder.build("Org", ['id', 'name', 'flex.date1', 'flex.text1', 'flex.num1'])
        MetaMapSchema mmSchema = MetaMapSchema.of(mmIncs)
        Map flatMap = mmSchema.flatten()

        then:
        flatMap
        mmSchema.props.keySet() == ['id', 'name', 'flex'] as Set

        def idSchemaDef = flatMap['id']
        Map idSchema = flatMap['id']
        idSchema.type == 'integer'
        idSchema.format == 'int64'
        idSchema.readOnly

        Map nameSchema = flatMap['name']
        nameSchema.type == 'string'
        nameSchema.maxLength == 100

        Map num1Schema = flatMap['flex.num1']
        num1Schema.type == 'number'

        Map text1Schema = flatMap['flex.text1']
        text1Schema.type == 'string'

        Map date1Schema = flatMap['flex.date1']
        date1Schema.type == 'string'
        date1Schema.format == "date-time"
    }


}
