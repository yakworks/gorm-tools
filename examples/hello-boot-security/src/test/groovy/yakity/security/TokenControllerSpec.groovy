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

package yakity.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult

import spock.lang.Specification

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

// full tests
@SpringBootTest(classes = [TestSpringApplication])
@AutoConfigureMockMvc
class TokenControllerSpec extends Specification {

    @Autowired MockMvc mockMvc

    // @WithMockUser // mock user should not be needed since we are doing basic auth
    void "Basic auth and bearer auth should give ok status and hello"() {
        when: "we login with basic auth"
        //does asserts too
        MvcResult result = mockMvc.perform(post("/token")
            .with(httpBasic("user", "123")))
            .andExpect(status().isOk())
            .andReturn()

        String token = result.getResponse().getContentAsString()

        then: "we can use the returned jwt as bearer"
        mockMvc.perform(get("/")
            .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
    }

    void "should be unauthroized"() {
        expect:
        mockMvc.perform(get("/"))
            .andExpect(status().isUnauthorized());
    }


    void "bad credential gives 401"() {
        expect:
        this.mockMvc.perform(post("/token"))
            .andExpect(status().isUnauthorized());
    }
}
