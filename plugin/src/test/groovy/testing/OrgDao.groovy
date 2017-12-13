package testing

import gorm.tools.dao.GormDao
import grails.artefact.Artefact
import grails.gorm.transactions.Transactional

@Artefact("Dao")
@Transactional
class OrgDao implements GormDao<Org> {

    void beforeCreate(Org org, Map params) {
        org.event = "beforeCreate"
    }

    void beforeUpdate(Org org, Map params) {
        org.event = "beforeUpdate"
    }

    void insertTestData() {
        (1..10).each { index ->
            String value = "Name#" + index
            new Org(id: index,
                name: value,
                isActive: (index % 2 == 0),
                amount: (index - 1) * 1.34,
                amount2: (index - 1) * (index - 1) * 0.3,
                date: new Date().clearTime() + index,
                secondName: index % 2 == 0 ? null : "Name2#" + index,
                location: (new Location(city: "City#$index", nested: new Nested(name: "Nested#${2 * index}", value: index)).persist())
            ).persist()
        }
    }

}
