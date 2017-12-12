package daoapp

class Address {
    String city
    Long testId
    static constraints = {
        testId nullable: true
    }
}
