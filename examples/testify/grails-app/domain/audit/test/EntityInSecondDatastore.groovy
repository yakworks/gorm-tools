package audit.test

import yakworks.security.auditable.Auditable

class EntityInSecondDatastore implements Auditable {

    String name
    Integer someIntegerProperty

    static constraints = {
    }

    static mapping = {
        //datasource("second")
    }
}
