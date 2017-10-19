package daoapp

class Org {
	String name
    Address address
    Date testDate
    boolean isActive=true
    BigDecimal revenue = 0
    Long refId = 0L
    static constraints = {
		name blank:false
        address nullable: true
        testDate nullable: true
    }
}
