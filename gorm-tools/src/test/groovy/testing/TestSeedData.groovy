package testing

import java.time.LocalDate
import java.time.LocalDateTime

import grails.buildtestdata.TestData

class TestSeedData {

    static void buildCustomers(int count){
        def type = TestData.build(CustType)
        (1..count).each { index ->
            String value = "Name$index"
            Integer descId = count-index+1
            new Cust(id: index, descId: descId,
                name: value,
                type: type,
                inactive: (index % 2 == 0),
                amount: (index - 1) * 1.34,
                amount2: (index - 1) * (index - 1) * 0.3,
                date: LocalDate.now().plusDays(index).toDate(),
                locDate: LocalDate.now().plusDays(index),
                locDateTime: LocalDate.now().plusDays(index).atStartOfDay(),
                name2: index % 2 == 0 ? null : "Name2-$index",
                kind: index % 2 == 0 ? Cust.Kind.CLIENT : Cust.Kind.COMPANY,
                testIdent: index % 2 == 0 ? TestIdent.Num2 : TestIdent.Num4,
                location: (
                    new Address(
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
