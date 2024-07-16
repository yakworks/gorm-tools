package audit.test

import yakworks.security.auditable.Auditable

class Heliport implements Auditable {
    String code
    String name

    static mapping = {
        version false
    }
}
