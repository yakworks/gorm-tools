package yakworks.testify


import gorm.tools.security.domain.AppUser
import gorm.tools.security.domain.SecRole
import gorm.tools.security.domain.SecRoleUser
import yakworks.gorm.testing.model.KitchenSeedData
import yakworks.gorm.testing.model.KitchenSink
import yakworks.rally.orgs.model.Org
import yakworks.rally.testing.RallySeedData

class BootStrap {

    def init = { servletContext ->
        buildKitchen()
    }

    def destroy = {
    }

    void buildKitchen(){
        KitchenSink.withTransaction {
            println "BootStrap inserting 100 customers"
            KitchenSeedData.buildKitchen(10)
        }
    }

}
