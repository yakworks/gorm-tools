package test

import yakworks.security.auditable.Auditable

class Author implements Auditable {
    String name
    Long age
    Boolean famous = false
    Publisher publisher

    // This should get masked globally
    String ssn = "123-456-7890"

    Date dateCreated
    Date lastUpdated
    String lastUpdatedBy

    // name, age, famous, publisher, ssn, dateCreated
    static int NUMBER_OF_AUDITABLE_PROPERTIES = 6

    static hasMany = [books: Book]

    static constraints = {
        lastUpdatedBy nullable: true
        publisher nullable: true
    }
}
