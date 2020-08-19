package yakworks.taskify.seed

import groovy.transform.CompileStatic

import yakworks.taskify.domain.Location
import yakworks.taskify.domain.Org
import yakworks.taskify.domain.OrgExt
import yakworks.taskify.domain.OrgStatus
import yakworks.taskify.domain.OrgType

// import grails.buildtestdata.TestData

@CompileStatic
class TestSeedData {

    static void buildOrgs(int count){
        def type = new OrgType(name: 'customer')
        type.id = 1 as Long
        type.persist()//TestData.build(OrgType)
        (1..2).each{
            def porg = createOrg(it , type)
            porg.kind = Org.Kind.PARENT
            porg.persist()
        }
        // createOrg(1, type, Org.Kind.PARENT).persist()
        // createOrg(2, type, Org.Kind.PARENT).persist()
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
            num: "$id",
            name: value,
            name2: id % 2 == 0 ? null : "OrgName2" + id,
            type: type,
            kind: (id % 2 == 0) ? Org.Kind.CLIENT : Org.Kind.VENDOR,
            status: (id % 2 == 0) ? OrgStatus.Active : OrgStatus.Inactive,
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
