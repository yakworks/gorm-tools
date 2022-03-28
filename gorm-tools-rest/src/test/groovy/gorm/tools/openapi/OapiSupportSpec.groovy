package gorm.tools.openapi

import java.nio.file.Paths

import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.NumberSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.parser.core.models.ParseOptions
import spock.lang.IgnoreRest
import spock.lang.Specification
import yakworks.commons.util.BuildSupport

class OapiSupportSpec extends Specification {

    Schema getSchema(String name){
        return getOapiSupport().getSchema(name)
    }
    OapiSupport getOapiSupport(){
        return OapiSupport.instance()
    }

    void "parse it sandbox"() {
        when:
        def oapiService = new OapiSupport()
        // OpenAPI openAPI = new OpenAPIV3Parser().read('oapi.yaml')
        // Schema contact = openAPI.getComponents().schemas['Contact']
        Schema contact = oapiService.getSchema('Contact')
        // Schema orgSchema1 = contact.$ref('org')

        Map contactProps = contact.properties
        Schema orgProp = contactProps.org

        // Schema orgSchema = resolveSchema(openAPI, 'org')

        contactProps.each { k, v ->
            if(v.class == Schema){
                def schema = oapiService.resolveSchema(v.$ref)
                println "$k -> Ref -> $schema.title"
            } else {
                println "$k -> Simple -> $v.type"
            }
        }

        then:
        contact
        // orgProp.properties

    }

    void "getSchemaForPath with simple props"() {
        when:
        def contactSchema = getSchema('Contact')
        def nameProp = oapiSupport.getSchemaForPath(contactSchema, 'name')
        def idProp = oapiSupport.getSchemaForPath(contactSchema, 'id')

        then:
        nameProp.class == StringSchema
        nameProp.type == 'string'

        idProp.class == IntegerSchema
        idProp.type == 'integer'
    }

    void "getSchemaForPath for ref"() {
        when:
        def contactSchema = getSchema('Contact')
        def prop = oapiSupport.getSchemaForPath(contactSchema, 'org')

        then:
        prop.class == Schema
    }

    void "getSchemaForPath with path key"() {
        when:
        def contactSchema = getSchema('Contact')
        def orgName = oapiSupport.getSchemaForPath(contactSchema, 'org.name')
        def orgFlexNum = oapiSupport.getSchemaForPath(contactSchema, 'org.flex.num1')

        then:
        orgName.class == StringSchema
        orgName.type == 'string'
        orgName.maxLength == 100

        orgFlexNum.class == NumberSchema
        orgFlexNum.type == 'number'
    }

}
