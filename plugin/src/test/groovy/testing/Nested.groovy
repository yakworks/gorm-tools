package testing

import grails.persistence.Entity

@Entity
class Nested {
    String name
    BigDecimal value

    static constraints = {
        name blank: true, nullable: true, examole: "test"
        value nullable: true, example: new BigDecimal(123)
    }
}
