package testing

import grails.persistence.Entity

@Entity
class Org {
    int id
    String name
    Boolean isActive = false
    BigDecimal amount
    BigDecimal amount2
    Location location
    String secondName
    Date date

    static List quickSearchFields = ["name"]

    static constraints = {
        name blank: true, nullable: true
        isActive nullable: true
        amount nullable: true
        secondName nullable: true
    }
}
