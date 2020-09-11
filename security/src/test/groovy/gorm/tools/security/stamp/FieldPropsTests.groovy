package gorm.tools.security.stamp

import java.time.LocalDateTime

import org.grails.config.PropertySourcesConfig
import org.grails.testing.GrailsUnitTest

import gorm.tools.security.audit.ast.FieldProps
import spock.lang.Specification

class FieldPropsTests extends Specification implements GrailsUnitTest {


    void test_buildFiledPropsMap() {
        when:
        ConfigObject co = new ConfigSlurper().parse(testConfig);

        then:
        assert co.grails.plugin.audittrail
        assert FieldProps.getMap(co, "grails.plugin.audittrail.createdBy")
        assert !FieldProps.getMap(co, "grails.plugin.audittrail.xxx")
        //assert co.getProperty("grails.plugin.audittrail")
        def fmap = FieldProps.buildFieldMap(new PropertySourcesConfig(co))
        assert fmap
        assert fmap.get("createdBy")
        assert fmap.get("createdBy").name == "blah"
        assert fmap.get("createdBy").type == Long

        // these come from defaults as they are not in the config
        assert fmap.get("createdDate").name == "createdDate"
        assert fmap.get("createdDate").type == LocalDateTime

        assert fmap.get("editedDate").type == java.time.Instant
        //FieldProps.buildFieldMap()
    }

    def testConfig = """
        grails{
            plugin{
                audittrail{
                    //this should have all the defaults
                    createdBy.field   = "blah"

                    editedBy.type   = "java.lang.String"
                    editedBy.constraints = "nullable:true,bindable:false"

                    editedDate.type  = "java.time.Instant"


                    currentUserClosure = {ctx->
                        return "RON"
                    }
                }
            }
        }

    """
}
