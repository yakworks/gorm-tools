package gorm.tools.openapi

import java.nio.file.Paths

import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.NumberSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.parser.core.models.ParseOptions
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.commons.util.BuildSupport

class SchemaViewSpec extends Specification {

    // Schema getSchema(String name){
    //     return getOapiSupport().getSchema(name)
    // }

    // OapiSupport getOapiSupport(){
    //     return OapiSupport.instance()
    // }

    void "Org sanity check"() {
        when:
        def sv = SchemaView.of('Org')
        sv.includes(['id', 'name', 'num']).build()
        def props = sv.props

        then:
        props
        sv.required == ['name', 'num', 'type']
        IntegerSchema idSchema = props['id']
        idSchema.type == 'integer'
        idSchema.format == 'int64'
        idSchema.readOnly

        StringSchema numSchema = props['num']
        numSchema.type == 'string'
        numSchema.maxLength == 50

        StringSchema nameSchema = props['name']
        nameSchema.type == 'string'
        nameSchema.maxLength == 100

    }

    @Ignore
    void "Org associations"() {
        when:
        def sv = SchemaView.of('Org')
        sv.includes(['id', 'num', 'flex.']).build()
        def props = sv.props

        then:

        props
        IntegerSchema idSchema = props['id']
        idSchema.type == 'integer'
        idSchema.format == 'int64'
        idSchema.readOnly

        StringSchema numSchema = props['num']
        numSchema.type == 'string'
        numSchema.maxLength == 50

        StringSchema nameSchema = props['name']
        nameSchema.type == 'string'
        nameSchema.maxLength == 100

    }

}
