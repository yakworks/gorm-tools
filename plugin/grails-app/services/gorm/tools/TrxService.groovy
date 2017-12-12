package gorm.tools

import grails.gorm.transactions.Transactional

/**
 * A service for running transactions. Using the @Transactional annotation and leaning on that to run the
 * trx seems to perform better than using the WithTransaction methods. This is mostly useful in Traits where we can't
 * use the @Transactional annotion and need to ensure certain methods are in a transaction.
 * @see gorm.tools.dao.GormDao
 */
@Transactional
class TrxService {

    public <T> T withTrx(Closure<T> callable) {
        return callable()
    }

}
