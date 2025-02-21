/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.openapi.gorm.meta

import org.springframework.util.SerializationUtils

import io.swagger.v3.oas.models.media.DateTimeSchema
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.NumberSchema
import io.swagger.v3.oas.models.media.StringSchema
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import yakworks.meta.MetaEntity
import yakworks.meta.MetaMap
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgFlex
import yakworks.testing.gorm.unit.GormHibernateTest

/**
 * sanity check test for service, which is just a wrapper so cacheable works
 */
class MetaEntitySchemaServiceSpec extends Specification implements GormHibernateTest {
    static entityClasses = [Org, OrgFlex, Location]

    @Shared
    def metaEntitySchemaService = new MetaEntitySchemaService()

    void "test buildIncludesMap simple"(){
        when:
        // MetaEntity mmIncs = MetaGormEntityBuilder.build("Org", ['id', 'num', 'name'])
        MetaEntity mmi = metaEntitySchemaService.getMetaEntity(Org.name, ['id', 'num', 'name'], [])

        then:
        mmi
        //sanity check
        mmi.className == 'yakworks.rally.orgs.model.Org'
        mmi.shortClassName == 'Org'
        mmi.schema
        mmi.metaProps.keySet() == ['id', 'num', 'name'] as Set

        mmi.metaProps.id.schema
        mmi.metaProps.num.schema
        mmi.metaProps.name.schema

        IntegerSchema idSchema = mmi.metaProps['id'].schema
        idSchema.type == 'integer'
        idSchema.format == 'int64'
        idSchema.readOnly

        StringSchema numSchema = mmi.metaProps['num'].schema
        numSchema.type == 'string'
        numSchema.maxLength == 50

        StringSchema nameSchema = mmi.metaProps['name'].schema
        nameSchema.type == 'string'
        nameSchema.maxLength == 100
    }

    void "test buildIncludesMap associations"(){
        when:
        MetaEntity mmi = metaEntitySchemaService.getMetaEntity(Org.name, ['id', 'name', 'flex.date1', 'flex.text1', 'flex.num1'], [])

        then:
        mmi
        //sanity check
        mmi.shortClassName == 'Org'
        mmi.metaProps.keySet() == ['id', 'name', 'flex'] as Set

        IntegerSchema idSchema = mmi.metaProps['id'].schema
        idSchema.type == 'integer'
        idSchema.format == 'int64'
        idSchema.readOnly

        StringSchema nameSchema = mmi.metaProps['name'].schema
        nameSchema.type == 'string'
        nameSchema.maxLength == 100

        def flexPropsMap = mmi.metaProps['flex'].metaProps
        NumberSchema num1Schema = flexPropsMap['num1'].schema
        num1Schema.type == 'number'

        StringSchema text1Schema = flexPropsMap['text1'].schema
        text1Schema.type == 'string'

        DateTimeSchema date1Schema = flexPropsMap['date1'].schema
        date1Schema.type == 'string'
        date1Schema.format == "date-time"
    }

    void "flattenSchema"(){
        when:
        MetaEntity ment = metaEntitySchemaService.getMetaEntity(Org.name, ['id', 'name', 'flex.date1', 'flex.text1', 'flex.num1'], [])
        Map flatMap = ment.flattenSchema()

        then:
        flatMap
        ment.metaProps.keySet() == ['id', 'name', 'flex'] as Set

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

    @Ignore
    void "test serialize"() {
        when: 'sanity check'
        MetaEntity ment = metaEntitySchemaService.getMetaEntity(Org.name, ['id', 'name', 'flex.date1', 'flex.text1', 'flex.num1'], [])

        def serialMent = SerializationUtils.serialize(ment)
        MetaEntity deserialMent = SerializationUtils.deserialize(serialMent) as MetaEntity

        then:
        noExceptionThrown()

        when:
        Map flatMap = deserialMent.flattenSchema()

        then:
        deserialMent.metaProps.keySet() == ['id', 'name', 'flex'] as Set
        Map idSchema = flatMap['id']
        idSchema.type == 'integer'
        idSchema.format == 'int64'
        idSchema.readOnly

        Map nameSchema = flatMap['name']
        nameSchema.type == 'string'
        nameSchema.maxLength == 100
    }

}
