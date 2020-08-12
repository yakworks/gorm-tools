package yakworks.taskify.seed

import groovy.transform.CompileStatic

import yakworks.taskify.domain.Location
import yakworks.taskify.domain.Org
import yakworks.taskify.domain.OrgExt
import yakworks.taskify.domain.OrgType

// import grails.buildtestdata.TestData

@CompileStatic
class TestSeedData {

    static void buildOrgs(int count){
        def type = new OrgType(name: 'customer')
        type.id = 1 as Long
        type.persist()//TestData.build(OrgType)
        createOrg(1 , type).persist()
        createOrg(2, type).persist()
        (3..count).each { id ->
            def org = createOrg(id, type)
            org.ext = new OrgExt(
                text1: "Ext$id",
                orgParent: id % 2 == 0 ? Org.get(1) : Org.get(2)
            )
            //org.ext.orgParent = id % 2 == 0 ? Org.get(1) : Org.get(2)
            org.persist()
        }
    }

    static Org createOrg(Long id, OrgType type){
        def loc = new Location(city: "City$id")
        loc.id = id
        loc.persist()

        String value = "Org" + id
        def org = new Org(
            name: value,
            name2: id % 2 == 0 ? null : "OrgName2" + id,
            type: type,
            inactive: (id % 2 == 0),
            revenue: (id - 1) * 1.25,
            creditLimit: (id - 1) * 1.5,
            actDate: new Date().clearTime() + (id as Integer),
            location: loc
        )
        org.id = id
        return org
    }

}
