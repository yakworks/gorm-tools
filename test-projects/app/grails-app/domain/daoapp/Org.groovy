package daoapp

class Org {
	String name
    Address address
    Date testDate
    static constraints = {
		name blank:false
        address nullable: true
        testDate nullable: true
    }
}
