package testing

import grails.buildtestdata.TestData

class TestSeedData {

    static void buildOrgs(int count){
        def type = TestData.build(OrgType)
        (1..count).each { index ->
            String value = "Name$index"
            Integer descId = count-index+1
            new Org(id: index, descId: descId,
                name: value,
                type: type,
                inactive: (index % 2 == 0),
                amount: (index - 1) * 1.34,
                amount2: (index - 1) * (index - 1) * 0.3,
                date: new Date().clearTime() + index,
                name2: index % 2 == 0 ? null : "Name2-$index",
                location: (
                    new Location(
                        address: "City$index",
                        nested: new Nested(
                            name: "Nested${2 * index}",
                            value: index
                        )
                    ).save()
                )
            ).save(failOnError: true)
        }
    }
}
