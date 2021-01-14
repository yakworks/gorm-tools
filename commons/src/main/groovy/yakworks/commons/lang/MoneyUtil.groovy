/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.lang

import java.math.RoundingMode

import groovy.transform.CompileStatic

@CompileStatic
class MoneyUtil {

    static BigDecimal roundedAmount(BigDecimal amount) {
        if (amount) {
            amount = amount.setScale(2, RoundingMode.HALF_UP)
        }
        return amount
    }

    /** Checks to see if the two numbers are within a half penny of each other.
     * @param a The first number.
     * @param b The second number.
     * @return true if they are within .005 of each other.
     */
    static boolean compareWithTolerance(BigDecimal a, BigDecimal b) {
        return (a - b).abs() <= 0.005
    }
}
