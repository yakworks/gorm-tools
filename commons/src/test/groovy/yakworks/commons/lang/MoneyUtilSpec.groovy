package yakworks.commons.lang

import spock.lang.Specification

class MoneyUtilSpec extends Specification {

    void testCompareWithTolerance(){
        expect:
        MoneyUtil.compareWithTolerance(2.001, 2.00)
        !MoneyUtil.compareWithTolerance(2.001, 2.008)
    }

}
