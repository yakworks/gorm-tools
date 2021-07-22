package yakworks.testify


import gorm.tools.security.domain.AppUser
import gorm.tools.security.domain.SecRole
import gorm.tools.security.domain.SecRoleUser
import yakworks.rally.orgs.model.Org
import yakworks.rally.testing.RallySeedData
import yakworks.testify.model.KitchenSink
import yakworks.testify.seed.TestSeedData

class BootStrap {

    def init = { servletContext ->
        buildOrgs()
        buildKitchen()
        buildAppUser()
    }

    def destroy = {
    }

    void buildOrgs(){
        Org.withTransaction {
            RallySeedData.buildOrgs(100)
        }
    }

    void buildKitchen(){
        KitchenSink.withTransaction {
            println "BootStrap inserting 100 customers"
            TestSeedData.buildKitchen(10)
        }
    }

    void buildAppUser(){
        AppUser.withTransaction {
            println "BootStrap inserting AppUser"
            AppUser user = new AppUser(id: 1, username: "admin", email: "admin@9ci.com", password:"123Foo")
            user.persist()
            //AppUser user = AppUser.create([id: 1, username: "admin", email: "admin@9ci.com", password:"admin"], bindId: true)
            assert user.id == 1

            SecRole admin = SecRole.create([id:1, name: SecRole.ADMINISTRATOR], bindId: true)
            SecRole power = SecRole.create([id:2, name: "Power User"], bindId: true)
            SecRole guest = SecRole.create([id:3, name: "Guest"], bindId: true)

            SecRoleUser.create(user, admin, true)
            SecRoleUser.create(user, power, true)

            AppUser noRoleUser = AppUser.create([id: 2, username: "noroles", email: "noroles@9ci.com", password:"123Foo"], bindId: true)
            assert noRoleUser.id == 2
            return
        }
    }
}
