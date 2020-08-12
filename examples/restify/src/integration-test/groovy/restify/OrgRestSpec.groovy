package restify

import geb.spock.GebSpec
import gorm.tools.rest.testing.RestApiFuncSpec
import gorm.tools.rest.testing.RestApiTestTrait
import gorm.tools.testing.TestDataJson
import grails.gorm.transactions.Rollback
import grails.gorm.transactions.Transactional
import grails.testing.mixin.integration.Integration
import yakworks.taskify.domain.Org
import yakworks.taskify.domain.OrgType

@Integration
@Rollback
class OrgRestSpec extends RestApiFuncSpec {

    String path = "api/org"

    //@Transactional
    //Map getPostData() { return TestDataJson.buildMap(Org, type: OrgType.load(1)) }
    Map postData = [name: "foo", type: [id: 1]]

    Map putData = [name: "Name Update"]

    Map invalidData = ["name": null]
    //
    // def "Check list"() {
    //     when:
    //     def response = restBuilder.get(resourcePath)
    //     then:
    //     response.json.data.size() == 10
    // }
    //
    // def "Filter by Name eq"() {
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list") {
    //         json([criteria: [name: "Org23"]])
    //     }.json
    //     then:
    //     list.size() == 1
    //     list[0].name == "Org23"
    // }
    //
    // def "Filter by id eq"() {
    //     given:
    //     Map data = [criteria: [id: 24]]
    //
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list") {
    //         json(data)
    //     }.json
    //     then:
    //     list.size() == 1
    //     list[0].name == "Org23"
    // }
    //
    // def "Filter by id inList"() {
    //     given:
    //     Map data = [criteria: [id: [24, 25]]]
    //
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list") {
    //         json(data)
    //     }.json
    //
    //     then:
    //     list.size() == 2
    //     list[0].name == "Org23"
    // }
    //
    // def "Filter by Name ilike"() {
    //     given:
    //     Map data = [criteria: [name: "Org2%"]]
    //
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list?max=150") {
    //         json(data)
    //     }.json
    //
    //     then:
    //     list.size() == 11
    //     list[0].name == "Org2"
    //     list[1].name == "Org20"
    //     list[10].name == "Org29"
    // }
    //
    // def "Filter by nested id"() {
    //     given:
    //     Map data = [criteria: [address: [id: 2]]]
    //
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list?max=150") {
    //         json(data)
    //     }.json
    //
    //     then:
    //     list.size() == 1
    //     list[0].name == "Org1"
    //     list[0].address.id == 2
    // }
    //
    // def "Filter by nestedId"() {
    //     given:
    //     Map data = [criteria: ["address.id": 2]]
    //
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list?max=150") {
    //         json(data)
    //     }.json
    //
    //     then:
    //     list.size() == 1
    //     list[0].name == "Org1"
    //     list[0].address.id == 2
    // }
    //
    // def "Filter by nested id inList"() {
    //     given:
    //     Map data = [criteria: [address: [id: [24, 25, 26]]]]
    //
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list?max=150") {
    //         json(data)
    //     }.json
    //
    //     then:
    //     list.size() == 3
    //     list[0].name == "Org23"
    // }
    //
    // def "Filter by nested string"() {
    //     given:
    //     Map data = [criteria: [address: [city: "City2"]]]
    //
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list?max=150") {
    //         json(data)
    //     }.json
    //
    //     then:
    //     list.size() == 1
    //     list[0].name == "Org2"
    //     list[0].address.id == 3
    // }
    //
    // def "Filter by nested string ilike"() {
    //     given:
    //     Map data = [criteria: [address: [city: "City2%"]]]
    //
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list?max=150") {
    //         json(data)
    //     }.json
    //
    //     then:
    //     list.size() == 11
    //     list[0].name == "Org2"
    //     list[1].name == "Org20"
    //     list[10].name == "Org29"
    //     list[0].address.id == 3
    //     list[1].address.id == 21
    //     list[10].address.id == 30
    // }
    //
    // def "Filter by boolean"() {
    //     given:
    //     Map data = [criteria: [isActive: true]]
    //
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list?max=150") {
    //         json(data)
    //     }.json
    //
    //     then:
    //     list.size() == Organisation.createCriteria().list() { eq "isActive", true }.size()
    // }
    //
    // def "Filter by boolean in list"() {
    //     given:
    //     Map data = [criteria: [isActive: [false]]]
    //
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list?max=150") {
    //         json(data)
    //     }.json
    //
    //     then:
    //     list.size() == 50
    //     list[0].isActive == false
    //     list[1].isActive == false
    // }
    //
    // def "Filter by BigDecimal"() {
    //     given:
    //     Map data = [criteria: [revenue: 200.0]]
    //
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list?max=150") {
    //         json(data)
    //     }.json
    //
    //     then:
    //     list.size() == 1
    //     list[0].name == "Org2"
    // }
    //
    // def "Filter by BigDecimal in list"() {
    //     given:
    //     Map data = [criteria: [revenue: [200.0, 500.0]]]
    //
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list?max=150") {
    //         json(data)
    //     }.json
    //
    //     then:
    //     list.size() == 2
    //     list[0].name == "Org2"
    //     list[1].name == "Org5"
    // }
    //
    // def "Filter by Date"() {
    //     given:
    //     Map data = [criteria: [testDate: (new Date() + 1).clearTime()]]
    //
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list?max=150") {
    //         json(data)
    //     }.json
    //
    //     then:
    //     list.size() == 1
    //     list[0].name == "Org1"
    // }
    //
    // def "Filter by Date le"() {
    //     given:
    //     Map data = [criteria: ['testDate.$lte': (new Date() + 1).clearTime()]]
    //
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list?max=150") {
    //         json(data)
    //     }.json
    //
    //     then:
    //     list.size() == Organisation.createCriteria().list() { le "testDate", (new Date() + 1).clearTime() }.size()
    //     list[0].name == Organisation.createCriteria().list() { le "testDate", (new Date() + 1).clearTime() }[0].name
    // }
    //
    // def "Filter by xxxId 1"() {
    //     given:
    //     Map data = [criteria: [refId: 200]]
    //
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list?max=150") {
    //         json(data)
    //     }.json
    //
    //     then:
    //     list.size() == 1
    //     list[0].name == "Org1"
    // }
    //
    // def "Filter by xxxId 2"() {
    //     given:
    //     Map data = [criteria: ["address.testId": 9]]
    //
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list?max=150") {
    //         json(data)
    //     }.json
    //     then:
    //     list.size() == 1
    //     list[0].name == "Org3"
    // }
    //
    //
    // def "Filter by xxxId 3"() {
    //     given:
    //     Map data = [criteria: [address: [testId: 3]]]
    //
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list?max=150") {
    //         json(data)
    //     }.json
    //
    //     then:
    //     list.size() == 1
    //     list[0].name == "Org1"
    // }
    //
    // def "Filter by xxxId 4"() {
    //     given:
    //     Map data = [criteria: ["address.testId": [9, 12]]]
    //
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list?max=150") {
    //         json(data)
    //     }.json
    //
    //     then:
    //     list.size() == 2
    //     list[0].name == "Org3"
    // }
    //
    //
    // def "Filter with `or` "() {
    //     given:
    //     Map data = [criteria: ['$or': ["name": "Org1", "address.id": 4]]]
    //
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list?max=150") {
    //         json(data)
    //     }.json
    //
    //     then:
    //     list.size() == 2
    //     list[0].name == "Org1"
    //     list[1].name == "Org3"
    // }
    //
    // def "Filter with `or` on low level"() {
    //     given:
    //     Map data = [criteria: [address: ['$or': ["city": "City1", "id": 4]]]]
    //
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list?max=150") {
    //         json(data)
    //     }.json
    //
    //     then:
    //     list.size() == 2
    //     list[0].name == "Org1"
    //     list[1].name == "Org3"
    // }
    //
    // def "Filter with `or` with like"() {
    //     given:
    //     Map data = [criteria: ["\$or": ["name": "Org2%", "address.id": 4]]]
    //
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list?max=150") {
    //         json(data)
    //     }.json
    //
    //     then:
    //     list.size() == 12
    //     list[0].name == "Org2"
    //     list[1].name == "Org3"
    //     list[2].name == "Org20"
    // }
    //
    // def "Filter with `between()`"() {
    //     given:
    //     Map data = [criteria: [id: ["\$between": [2, 10]]]]
    //
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list?max=150") {
    //         json(data)
    //     }.json
    //
    //     then:
    //     list.size() == 9
    //     list[0].name == "Org1"
    //     list[1].name == "Org2"
    //     list[-1].name == "Org9"
    // }
    //
    // def "Filter with `in()`"() {
    //     given:
    //     Map data = [criteria: [id: ["\$in": [24, 25]]]]
    //
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list?max=150") {
    //         json(data)
    //     }.json
    //
    //     then:
    //     list.size() == 2
    //     list[0].name == "Org23"
    // }
    //
    // def "Filter with `inList()`"() {
    //     given:
    //     Map data = [criteria: [id: ["\$inList": [24, 25]]]]
    //
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list?max=150") {
    //         json(data)
    //     }.json
    //
    //     then:
    //     list.size() == 2
    //     list[0].name == "Org23"
    // }
    //
    //
    // def "Filter by Name ilike()"() {
    //     given:
    //     Map data = [criteria: [name: ["\$ilike": "Org2%"]]]
    //
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list?max=150") {
    //         json(data)
    //     }.json
    //
    //     then:
    //     list.size() == 11
    //     list[0].name == "Org2"
    //     list[1].name == "Org20"
    //     list[10].name == "Org29"
    // }
    //
    // def "test paging, defaults"() {
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list").json
    //
    //     then:
    //     list.size() == 10
    // }
    //
    // def "test paging"() {
    //     when:
    //     def envelope = restBuilder.get(resourcePath + "?max=20").json
    //     then:
    //     envelope.data.size() == 20
    //     envelope.data[0].id == 1
    //
    //     when:
    //     envelope = restBuilder.get(resourcePath + "?page=2").json
    //     then:
    //     envelope.data.size() == 10
    //     envelope.data[0].id == 11
    //
    // }
    //
    // def "test quick search"() {
    //     given:
    //     Map data = [criteria: ['$qSearch': "Org2%"]]
    //
    //     when:
    //     List list = restBuilder.post(resourcePath + "/list?max=150") {
    //         json(data)
    //     }.json
    //     then:
    //     list.size() == 11
    //
    // }
    //
    // def "test sort"() {
    //     when:
    //     def envelope = restBuilder.get(resourcePath + "?sort=id&order=asc").json
    //     then:
    //     envelope.data[0].id == 1
    //
    //     when:
    //     envelope = restBuilder.get(resourcePath + "?sort=id&order=desc").json
    //     then:
    //     envelope.data[0].id == 100
    //
    //     when:
    //     envelope = restBuilder.get(resourcePath + "?sort=credit asc, id desc").json
    //     then:
    //     envelope.data*.id == [91, 81, 71, 61, 51, 41, 31, 21, 11, 1]
    //
    // }

}
