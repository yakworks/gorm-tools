package repoapp

class Org {
    String name
    String num
    Address address
    Date testDate
    boolean isActive = true
    BigDecimal revenue = 0
    BigDecimal credit
    Long refId = 0L
    String event

    static quickSearchFields = ["name", "num"]
    static constraints = {
        name blank: false
        num nullable: true
        address nullable: true
        testDate nullable: true
        credit nullable: true
        event nullable: true, bindable:false
    }
}
