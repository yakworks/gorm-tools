/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.common

import java.time.LocalDateTime

import groovy.transform.CompileStatic

@SuppressWarnings(['MethodName'])
@CompileStatic
trait FlexTrait {

    String text1
    String text2
    String text3
    String text4
    String text5
    String text6
    String text7
    String text8
    String text9
    String text10
    BigDecimal num1
    BigDecimal num2
    BigDecimal num3
    BigDecimal num4
    BigDecimal num5
    BigDecimal num6
    LocalDateTime date1
    LocalDateTime date2
    LocalDateTime date3
    LocalDateTime date4

    static constraintsMap = [
        text1: [d: "Flexible text field", maxSize: 255],
        text2: [d: "Flexible text field", maxSize: 255],
        text3: [d: "Flexible text field", maxSize: 255],
        text4: [d: "Flexible text field", maxSize: 255],
        text5: [d: "Flexible text field", maxSize: 255],
        text6: [d: "Flexible text field", maxSize: 255],
        text7: [d: "Flexible text field", maxSize: 255],
        text8: [d: "Flexible text field", maxSize: 255],
        text9: [d: "Flexible text field", maxSize: 255],
        text10: [d: "Flexible text field", maxSize: 255],
        num1: [d: "Flexible numeric field"],
        num2: [d: "Flexible numeric field"],
        num3: [d: "Flexible numeric field"],
        num4: [d: "Flexible numeric field"],
        num5: [d: "Flexible numeric field"],
        num6: [d: "Flexible numeric field"],
        date1: [d: "Flexible date field"],
        date2: [d: "Flexible date field"],
        date3: [d: "Flexible date field"],
        date4: [d: "Flexible date field"]
    ]

}
