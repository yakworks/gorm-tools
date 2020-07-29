package security

import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@GrailsCompileStatic
@EqualsAndHashCode(includes = 'username')
@ToString(includes = 'username', includeNames = true, includePackage = false)
class User implements Serializable {
    private static final long serialVersionUID = 1

    String username
    String password
    boolean enabled = true
    boolean accountExpired = false
    boolean accountLocked = false
    boolean passwordExpired = false

    Set<SecurityRole> getAuthorities() {
        (UserSecurityRole.findAllByUser(this) as List<UserSecurityRole>)*.securityRole as Set<SecurityRole>
    }

    static constraints = {
        username title: 'User Name', example: "Bob",
                blank: false, unique: true, nullable: false
        password example: "b4d_p455w0rd", blank: false, password: true, nullable: false
        enabled description: 'Is user active', nullable: false
        accountExpired description: 'Has user account expired', nullable: false
        accountLocked description: 'User account has been locked', nullable: false
        passwordExpired description: 'Password has expired', nullable: false
    }

    static mapping = {
        password column: '`password`'
        accountExpired defaultValue: "0"
        accountLocked defaultValue: "0"
        passwordExpired defaultValue: "0"
    }
}
