package audit.test

import yakworks.security.auditable.AuditEventType
import yakworks.security.auditable.Auditable

class Resolution implements Auditable {

	String name

	@Override
	Collection<AuditEventType> getLogIgnoreEvents() {
        [AuditEventType.UPDATE, AuditEventType.INSERT]
	}

	static constraints = {
	}
}
