package audit.test

import yakworks.security.auditable.Auditable

class Tunnel implements Auditable {
    String name
    String description

    static constraints = {
        description maxSize:1073741824, nullable:true
    }
}
