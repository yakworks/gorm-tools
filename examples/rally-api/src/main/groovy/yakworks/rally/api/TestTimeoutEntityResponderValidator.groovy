package yakworks.rally.api

import org.hibernate.QueryTimeoutException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

import gorm.tools.mango.api.QueryArgs
import yakworks.rest.gorm.responder.EntityResponderValidator

/*
 * Allows to test query timeout
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
class TestTimeoutEntityResponderValidator implements EntityResponderValidator {

    @Override
    QueryArgs validate(QueryArgs qargs) {
        //throw timeout if param is present in qarg
        if(qargs.qCriteria['timeout']) {
            throw new QueryTimeoutException("Test query timeout", null, null)
        }
    }
}
