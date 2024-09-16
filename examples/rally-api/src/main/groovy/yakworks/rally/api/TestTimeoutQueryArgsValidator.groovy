package yakworks.rally.api

import org.hibernate.QueryTimeoutException

import gorm.tools.mango.api.QueryArgs
import yakworks.security.gorm.api.UserQueryArgsValidator

/*
 * Overrides to test the query timeout
 */
class TestTimeoutQueryArgsValidator extends UserQueryArgsValidator {

    @Override
    QueryArgs validate(QueryArgs qargs) {
        //throw timeout if param is present in qarg
        if(qargs.qCriteria['timeout']) {
            throw new QueryTimeoutException("Test query timeout", null, null)
        }
        super.validate(qargs)
    }
}
