package gorm.tools.repository.errors

import groovy.transform.CompileStatic
import org.hibernate.ObjectNotFoundException
import org.springframework.dao.DataRetrievalFailureException

/**
 * an extension of the DataRetrievalFailureException that is more performant. fillInStackTrace is overriden to show nothing
 * so it will be faster and consume less memory when thrown.
 */
@CompileStatic
class EntityNotFoundException extends DataRetrievalFailureException {

    public EntityNotFoundException(String msg) {
        super(msg)
    }

    public EntityNotFoundException(Serializable id, String domainName) {
        super("${domainName} not found with id ${id}")
    }

    /**
     * Constructor for DataRetrievalFailureException.
     * @param msg the detail message
     * @param cause the root cause from the data access API in use
     */
    public EntityNotFoundException(String msg, Throwable cause) {
        super(msg, cause)
    }

    //Override it for performance improvement, because filling in the stack trace is quit expensive
    @SuppressWarnings(['SynchronizedMethod'])
    @Override
    synchronized Throwable fillInStackTrace() {}
}
