package yakworks.taskify

import skydive.Jumper
import yakworks.taskify.domain.Location
import yakworks.taskify.domain.OrgType
import yakworks.taskify.seed.TestSeedData

class BootStrap {

    def init = { servletContext ->
        OrgType.withTransaction {
            //new Jumper(name: 'Bill').persist()
            //new Location(street: '123').persist()
            //new OrgType(name: 'foo').persist()
            TestSeedData.buildOrgs(100)
        }
    }
    def destroy = {
    }
}
