package yakworks.security.shiro


import org.apache.shiro.authz.permission.WildcardPermission

import spock.lang.Specification

class WilcardPermissionSpec extends Specification {

    def "playground"() {
        when:
        //this is what our code would check against
        def permCheck = new WildcardPermission("rally:org:read");
        //these are what a user role would have setup
        def rolePerm1 = new WildcardPermission("rally:org:*");
        def rolePerm2 = new WildcardPermission("rally:org:read");
        def rolePerm3 = new WildcardPermission("rally:*:*");
        def rolePerm4 = new WildcardPermission("rally:*");
        def rolePerm5 = new WildcardPermission("rally:*:read");
        //should fail
        def rolePerm6 = new WildcardPermission("rally:*:create");

        then:
        rolePerm1.implies(permCheck)
        rolePerm2.implies(permCheck)
        rolePerm3.implies(permCheck)
        rolePerm4.implies(permCheck)
        rolePerm5.implies(permCheck)
        //not allowed
        !rolePerm6.implies(permCheck)
    }

}
