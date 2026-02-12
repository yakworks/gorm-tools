package audit.test

import yakworks.security.auditable.Auditable

class Review implements Auditable {
    String name
    Book book

    /**
     * Override entity id to use a nested entityId from another domain object
     * @return
     */
    @Override
    String getLogEntityId() {
        "${name}|${book.logEntityId}"
    }

    static constraints = {
    }
}
