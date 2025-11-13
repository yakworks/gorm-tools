package yakworks.security.shiro


import org.apache.shiro.authz.permission.WildcardPermission
import spock.lang.Ignore
import spock.lang.Specification

class WilcardPermissionSpec extends Specification {

    void "playground"() {
        when:
        //this is what our code would check against
        def permOrgUpdate = new WildcardPermission("rally:org:update");
        def permOrgUpdateSingleInstance = new WildcardPermission("rally:org:1:update")
        def permRpc = new WildcardPermission("rally:org:rpc:rpc1")

        def permBulkCreate = "rally:org:bulk:create"
        def permBulkUpdate = "rally:org:bulk:update"

        //these are what a user role would have setup
        def superAdmin = new WildcardPermission("*")
        def permAllOnOrg = new WildcardPermission("rally:org:*")
        def permAllOnRally = new WildcardPermission("rally:*:*")
        def permAllOnRallyOneLevel = new WildcardPermission("rally:*")
        def permOrgUpdateTest = new WildcardPermission("rally:org:update")
        def permRallyUpdateAny = new WildcardPermission("rally:*:update")
        def permOrgReadUpdate = new WildcardPermission("rally:org:read,update")

        //should reject
        def permOrgCreate = new WildcardPermission("rally:org:create")
        def permRallyCreateAny = new WildcardPermission("rally:*:create")

        then: "super admin can do all"
        assertImplies(superAdmin, permOrgUpdate)
        assertImplies(superAdmin, permOrgUpdateSingleInstance)
        assertImplies(superAdmin, permRpc)

        and: "rally:org:*"
        assertImplies(permAllOnOrg, permOrgUpdate)
        assertImplies(permAllOnOrg, permOrgUpdateSingleInstance)
        assertImplies(permAllOnOrg, permRpc)

        and: "rally:*"
        assertImplies(permAllOnRallyOneLevel, permOrgUpdate)
        assertImplies(permAllOnRallyOneLevel, permOrgUpdateSingleInstance)
        assertImplies(permAllOnRallyOneLevel, permRpc)

        and: "rally:org:update"
        assertImplies(permOrgUpdateTest, permOrgUpdate)
        !assertImplies(permOrgUpdateTest, permOrgUpdateSingleInstance)
        !assertImplies(permOrgUpdateTest, permRpc)

        and: "rally:org:update"
        permRallyUpdateAny.implies(permOrgUpdate)
        permOrgReadUpdate.implies(permOrgUpdate)
        //not allowed
        !permOrgCreate.implies(permOrgUpdate)
        !permRallyCreateAny.implies(permOrgUpdate)

        and: "rally:*:update"
        assertImplies(permRallyUpdateAny, permOrgUpdate)
        !assertImplies(permRallyUpdateAny, permOrgUpdateSingleInstance)  //rally:*:update does not work on rally:org:1:update
        !assertImplies(permRallyUpdateAny, permRpc)

        and: "verify bulk"
        assertImplies(superAdmin, permBulkCreate)
        assertImplies(superAdmin, permBulkUpdate)
        assertImplies(permAllOnRally, permBulkCreate)
        assertImplies(permAllOnRally, permBulkUpdate)
    }


    boolean assertImplies(def p1, def p2) {
        WildcardPermission perm1 = (p1 instanceof WildcardPermission) ? p1 : new WildcardPermission(p1)
        WildcardPermission perm2 = (p2 instanceof WildcardPermission) ? p2 : new WildcardPermission(p2)
        return perm1.implies(perm2)
    }

    /**
     *  ar:customer:* - will cover bulk
     *  ar:tran:create,read,update
     *  ar:tran:bulk:* - url = PUT /ar/tran/bulk
     *  ar:tran:rpc:imageUrl
     *  can do every thing else with AR
     *  cant do glVoid
     *  ar:bankAccount:read   (bankaccounts woulld be readonly)
     *
     *  autocash:batch:*
     *  autocash:payment:*
     *  autocash:paymentDetail:*
     */
    void "mulesoft example"() {
        setup:
        def anyOnCustomer = new WildcardPermission("ar:customer:*")
        def tranPerms =  new WildcardPermission("ar:tran:create,read,update")
        def tranBulk = new WildcardPermission("ar:tran:bulk:*")
        def tranRpc = new WildcardPermission("ar:tran:rpc:imageUrl,glVoid")
        def bankAccountReadonly =  new WildcardPermission("ar:bankAccount:read")

        def allOnBatch = new WildcardPermission("autocash:batch:*")
        def allOnPayment = new WildcardPermission("autocash:payment:*")
        def allOnPd = new WildcardPermission("autocash:paymentDetail:*")

        expect:
        assertImplies(anyOnCustomer, "ar:customer:read,create,update,delete")
        assertImplies(tranPerms, "ar:tran:create")
        assertImplies(tranBulk, "ar:tran:bulk:create,update")
        assertImplies(tranRpc, "ar:tran:rpc:glVoid")
        assertImplies(bankAccountReadonly, "ar:bankAccount:read")
        !assertImplies(bankAccountReadonly, "ar:bankAccount:create")
        !assertImplies(bankAccountReadonly, "ar:bankAccount:update")
        !assertImplies(bankAccountReadonly, "ar:bankAccount:delete")

        assertImplies(allOnBatch, "autocash:batch:read,create,update,delete")
        assertImplies(allOnPayment, "autocash:payment:read,create,update,delete")
        assertImplies(allOnPd, "autocash:paymentDetail:read,create,update,delete")
    }

}
