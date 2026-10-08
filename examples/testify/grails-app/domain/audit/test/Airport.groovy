package audit.test

import yakworks.security.auditable.Auditable

class Airport implements Auditable {
    String code
    String name

    static hasMany = [runways: Runway]
}
