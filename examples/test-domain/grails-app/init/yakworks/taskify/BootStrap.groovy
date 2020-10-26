package yakworks.taskify


import gorm.tools.security.domain.AppUser
import gorm.tools.security.domain.SecRole
import gorm.tools.security.domain.SecRoleUser
import yakworks.taskify.domain.Org
import yakworks.taskify.seed.TestSeedData

class BootStrap {

    def init = { servletContext ->
        Org.withTransaction {
            //new Jumper(name: 'Bill').persist()
            //new Location(street: '123').persist()
            //new OrgType(name: 'foo').persist()
            TestSeedData.buildOrgs(100)
        }
        AppUser.withTransaction {
            AppUser user = AppUser.create([id: 1, username: "admin", email: "admin@9ci.com", password:"admin"], bindId: true)
            assert user.id == 1

            SecRole admin = SecRole.create([id:1, name: SecRole.ADMINISTRATOR], bindId: true)
            SecRole power = SecRole.create([id:2, name: "Power User"], bindId: true)
            SecRole guest = SecRole.create([id:3, name: "Guest"], bindId: true)

            SecRoleUser.create(user, admin, true)
            SecRoleUser.create(user, power, true)

            AppUser noRoleUser = AppUser.create([id: 2, username: "noroles", email: "noroles@9ci.com", password:"admin"], bindId: true)
            assert noRoleUser.id == 2
            return
        }
    }
    def destroy = {
    }
}
