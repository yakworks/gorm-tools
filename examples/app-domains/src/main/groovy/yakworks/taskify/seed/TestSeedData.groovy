package yakworks.taskify.seed

import yakworks.taskify.domain.Location
import yakworks.taskify.domain.Org
import yakworks.taskify.domain.OrgType

// import grails.buildtestdata.TestData

class TestSeedData {

    static void buildOrgs(int count){
        def type = new OrgType(name: 'cust').persist()//TestData.build(OrgType)
        (1..count).each { index ->
            String value = "Org" + index
            new Org(id: index,
                name: value,
                name2: index % 2 == 0 ? null : "OrgName2" + index,
                type: type,
                inactive: (index % 2 == 0),
                revenue: (index - 1) * 1.34,
                actDate: new Date().clearTime() + index,
                location: (
                    new Location(
                        city: "City$index"
                    ).persist())
            ).persist()
        }
    }
}
