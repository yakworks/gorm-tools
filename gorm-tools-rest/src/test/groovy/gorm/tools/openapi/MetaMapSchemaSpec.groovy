/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.openapi

import yakworks.meta.MetaMapIncludes
import gorm.tools.metamap.MetaMapIncludesBuilder
import gorm.tools.testing.unit.DataRepoTest
import io.swagger.v3.oas.models.media.DateTimeSchema
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.NumberSchema
import io.swagger.v3.oas.models.media.StringSchema
import spock.lang.Specification
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgFlex

class MetaMapSchemaSpec extends Specification implements DataRepoTest {

    void setupSpec() {
        //mockDomain Person
        mockDomains Org, OrgFlex, Location
    }

    void "sanity check MetaMapIncludesBuilder.build"(){
        when:
        def res = MetaMapIncludesBuilder.build("Org", ['name'])

        then:
        res.className == 'yakworks.rally.orgs.model.Org'
        res.shortClassName == 'Org'
        res.propsMap.keySet() == ['name'] as Set

        when: "check on collections"
        res = MetaMapIncludesBuilder.build(Org, ['name', 'locations.city'])

        then:
        res.className.contains('Org') // [className: 'Bookz', props: ['name']]
        res.propsMap.keySet() == ['name', 'locations'] as Set
        res.nestedIncludes.size() == 1

        def itemsIncs = res.nestedIncludes['locations']
        itemsIncs.className == 'yakworks.rally.orgs.model.Location'
        itemsIncs.shortClassName == 'Location'
        itemsIncs.propsMap.keySet() == ['city'] as Set

    }

    void "test buildIncludesMap simple"(){
        when:
        MetaMapIncludes mmIncs = MetaMapIncludesBuilder.build("Org", ['id', 'num', 'name'])
        MetaMapSchema mmSchema = MetaMapSchema.of(mmIncs)

        then:
        mmSchema
        //sanity check
        mmSchema.shortRootClassName == 'Org'
        mmSchema.rootClassPropName == 'org'
        mmIncs.propsMap.keySet() == ['id', 'num', 'name'] as Set

        mmSchema.props.keySet() == ['id', 'num', 'name'] as Set

        IntegerSchema idSchema = mmSchema.props['id']
        idSchema.type == 'integer'
        idSchema.format == 'int64'
        idSchema.readOnly

        StringSchema numSchema = mmSchema.props['num']
        numSchema.type == 'string'
        numSchema.maxLength == 50

        StringSchema nameSchema = mmSchema.props['name']
        nameSchema.type == 'string'
        nameSchema.maxLength == 100
    }

    void "test buildIncludesMap associations"(){
        when:
        MetaMapIncludes mmIncs = MetaMapIncludesBuilder.build("Org", ['id', 'name', 'flex.date1', 'flex.text1', 'flex.num1'])
        MetaMapSchema mmSchema = MetaMapSchema.of(mmIncs)

        then:
        mmSchema
        //sanity check
        mmIncs.shortClassName == 'Org'
        mmIncs.propsMap.keySet() == ['id', 'name', 'flex'] as Set

        mmSchema.props.keySet() == ['id', 'name', 'flex'] as Set

        IntegerSchema idSchema = mmSchema.props['id']
        idSchema.type == 'integer'
        idSchema.format == 'int64'
        idSchema.readOnly

        StringSchema nameSchema = mmSchema.props['name']
        nameSchema.type == 'string'
        nameSchema.maxLength == 100

        Map flexMetaMap = mmSchema.props['flex']
        NumberSchema num1Schema = flexMetaMap['num1']
        num1Schema.type == 'number'
        StringSchema text1Schema = flexMetaMap['text1']
        text1Schema.type == 'string'
        DateTimeSchema date1Schema = flexMetaMap['date1']
        date1Schema.type == 'string'
        date1Schema.format == "date-time"
    }

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
