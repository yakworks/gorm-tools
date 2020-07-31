package security

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import grails.compiler.GrailsCompileStatic

import static grails.gorm.hibernate.mapping.MappingBuilder.*

@GrailsCompileStatic
@EqualsAndHashCode(includes = 'authority')
@ToString(includes = 'authority', includeNames = true, includePackage = false)
class SecurityRole implements Serializable {

    private static final long serialVersionUID = 1
    String authority

    static constraints = {
        authority description: 'The role name', example: 'ROLE_ADMIN',
                blank: false, unique: true
    }

    static mapping = orm {
        cache "read-write"
    }
}
