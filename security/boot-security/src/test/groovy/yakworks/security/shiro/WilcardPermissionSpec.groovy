package yakworks.security.shiro


import org.apache.shiro.authz.permission.WildcardPermission

import spock.lang.Specification

class WilcardPermissionSpec extends Specification {

    void "playground"() {
        when:
        //this is what our code would check against
        def permCheck = new WildcardPermission("rally:org:update");
        def permCheckOnItem = new WildcardPermission("rally:org:1:update")
        def permCheckOnRpc = new WildcardPermission("rally:org:rpc:rpc1")

        //these are what a user role would have setup
        def rolePerm1 = new WildcardPermission("rally:org:*")
        def rolePerm2 = new WildcardPermission("rally:*:*")
        def rolePerm3 = new WildcardPermission("rally:*")
        def rolePerm4 = new WildcardPermission("rally:org:update")
        def rolePerm5 = new WildcardPermission("rally:*:update")
        def rolePerm6 = new WildcardPermission("rally:org:read,update")

        //should reject
        def rolePerm7 = new WildcardPermission("rally:org:create")
        def rolePerm8 = new WildcardPermission("rally:*:create")

        then:
        rolePerm1.implies(permCheck)
        rolePerm2.implies(permCheck)
        rolePerm3.implies(permCheck)
        rolePerm4.implies(permCheck)
        rolePerm5.implies(permCheck)
        rolePerm6.implies(permCheck)
        //not allowed
        !rolePerm7.implies(permCheck)
        !rolePerm8.implies(permCheck)

        and:
        rolePerm1.implies(permCheckOnItem)
        rolePerm2.implies(permCheckOnItem)
        rolePerm3.implies(permCheckOnItem)
        rolePerm3.implies(permCheckOnItem)

        and: "does not apply on specific item"
        !rolePerm4.implies(permCheckOnItem)
        !rolePerm5.implies(permCheckOnItem)
        !rolePerm6.implies(permCheckOnItem)

        and: "Verify rpc"
        rolePerm1.implies(permCheckOnRpc)
        !rolePerm4.implies(permCheckOnRpc)
    }

}
