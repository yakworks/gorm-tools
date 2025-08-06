package audit.test

import yakworks.security.auditable.AuditEventType
import yakworks.security.auditable.Auditable

class Publisher implements Auditable {
    String code
    String name

    boolean active = false

    @Override
    String getLogEntityId() {
        "${code}|${name}"
    }

    @Override
    boolean isAuditable(AuditEventType eventType) {
        active
    }

    static constraints = {
    }
}
