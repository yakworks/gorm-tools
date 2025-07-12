/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.databinding


import spock.lang.Specification

class BasicDataBinderSpec extends Specification {

    void "test bind "() {
        when:
        def params = [
            bingo: 'was',
            bingo2: 'his',
            bingo3: 'nameOh',
            num: '123',
            bingos: 'was,his'
        ]
        //def basicDataBinder = new BasicDataBinder()
        def obj = BasicDataBinder.bind(new VanillaTarget(), params)

        then:
        obj.bingo == Bingo.WAS
        obj.bingo2 == Bingo.His
        obj.bingo3 == Bingo.NAME_oh
        obj.num == 123
        obj.bingos == [Bingo.WAS,Bingo.His]
    }

}

class VanillaTarget {

    Bingo bingo
    Bingo bingo2
    Bingo bingo3
    List<Bingo> bingos
    Integer num

}

enum Bingo {

    WAS, His, NAME_oh

}
