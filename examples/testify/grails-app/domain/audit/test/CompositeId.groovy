package audit.test

import yakworks.security.auditable.Auditable

class CompositeId implements Auditable, Serializable {

    Author author
    String string
    NonAuditableCompositeId nonAuditableCompositeId

    String notIdString

    static constraints = {
    }

    static mapping = {
        id composite:['author', 'string', 'nonAuditableCompositeId']
    }
}
