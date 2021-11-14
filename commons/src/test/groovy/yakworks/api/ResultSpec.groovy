/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.api

import spock.lang.Specification
import yakworks.commons.map.Maps

class ResultSpec extends Specification {

    void "simple Ok"(){
        when:
        def okRes = Result.OK()

        then:
        okRes.status == 200
        okRes.title('foo').title == 'foo'
        okRes.code('bar').args([key1:'go']).code == 'bar'
        okRes.args == [key1:'go']
    }

    // def "simple creation"(){
    //     expect:
    //     def okRes = Result.of
    //
    //     where:
    //     assertMapsEqual(m0, Maps.merge(m0))
    // }

}
