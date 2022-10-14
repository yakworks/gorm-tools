/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package yakworks.rally.api

import org.springframework.boot.test.context.SpringBootTest

import spock.lang.Ignore
import spock.lang.Specification

// @ContextConfiguration(
//     loader = GrailsApplicationContextLoader.class,
//     classes = [Application.class]
// )
@SpringBootTest
@Ignore //not working now for unit, No qualifying bean of type 'org.grails.datastore.mapping.model.MappingContext'
class SmokeTest extends Specification {

    void "smoke test spring boot startup"() {
        expect:
        "1" == '1'
    }
}
