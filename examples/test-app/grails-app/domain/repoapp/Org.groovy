package repoapp

import java.time.LocalDateTime

class Org {
    String name
    String num
    Address address
    Date testDate
    LocalDateTime testDateTwo
    boolean isActive = true
    BigDecimal revenue = 0
    BigDecimal credit
    Long refId = 0L
    String event

    static qSearchFields = ["name", "num"]

    static constraints = {
        name blank: false
        num nullable: true
        address nullable: true
        testDate nullable: true
        testDateTwo nullable: true
        credit nullable: true
        event nullable: true
    }
}
