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

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc

import grails.boot.test.GrailsApplicationContextLoader
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest
class SecurityMvcSpec extends Specification {

    @Autowired MockMvc mockMvc;

    void "should be unauthroized"() {
        expect:
        // status.value(), result.getResponse().getStatus());
        this.mockMvc.perform(get("/"))
            .andExpect(status().isUnauthorized())

        this.mockMvc.perform(get("/spring"))
            .andExpect(status().isUnauthorized())

        //about is open
        // this.mockMvc.perform(get("/about"))
        //     .andExpect(status().isOk())
    }

    @WithMockUser
    void "with mock userd"() {
        expect:
        this.mockMvc.perform(get("/"))
            .andExpect(status().isOk());
        this.mockMvc.perform(get("/spring"))
            .andExpect(status().isOk());
    }
}
