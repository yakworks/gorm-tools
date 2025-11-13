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
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(classes = [TestSpringApplication])
@AutoConfigureMockMvc
class AclPermissionSpec extends Specification {

    @Autowired MockMvc mockMvc

    void "should be unauthroized"() {
        expect:
        // status.value(), result.getResponse().getStatus());
        this.mockMvc.perform(get("/api/acl/test/1")).andExpect(status().isUnauthorized())
        this.mockMvc.perform(post( "/api/acl/test/")).andExpect(status().isUnauthorized())
        this.mockMvc.perform(put( "/api/acl/test/")).andExpect(status().isUnauthorized())
        this.mockMvc.perform(delete( "/api/acl/test/1")).andExpect(status().isUnauthorized())
    }

    //user has acl:foo:* but not acl:test:*
    @WithMockUser(authorities = ['acl:foo:*'])
    void "should be forbidden"() {
        expect:
        // status.value(), result.getResponse().getStatus());
        this.mockMvc.perform(get("/api/acl/test/1")).andExpect(status().isForbidden())
        this.mockMvc.perform(post( "/api/acl/test/")).andExpect(status().isForbidden())
        this.mockMvc.perform(put( "/api/acl/test/")).andExpect(status().isForbidden())
        this.mockMvc.perform(delete( "/api/acl/test/1")).andExpect(status().isForbidden())
    }

    @WithMockUser(authorities = ['acl:test:read'])
    void "read check"() {
        expect:
        this.mockMvc.perform(get("/api/acl/test/")).andExpect(status().isOk());
    }

    @WithMockUser(authorities = ['acl:test:create'])
    void "post check"() {
        expect:
        this.mockMvc.perform(post("/api/acl/test/")).andExpect(status().isOk());
    }

    @WithMockUser(authorities = ['acl:test:update'])
    void "put check"() {
        expect:
        this.mockMvc.perform(put("/api/acl/test/")).andExpect(status().isOk());
    }

    @WithMockUser(authorities = ['acl:test:delete'])
    void "delete check"() {
        expect:
        this.mockMvc.perform(delete("/api/acl/test/")).andExpect(status().isOk());
    }

    @WithMockUser(authorities = ['acl:test:*'])
    void "wildcard check"() {
        expect:
        this.mockMvc.perform(get("/api/acl/test/")).andExpect(status().isOk());
        this.mockMvc.perform(post("/api/acl/test/")).andExpect(status().isOk());
        this.mockMvc.perform(put("/api/acl/test/")).andExpect(status().isOk());
        this.mockMvc.perform(delete("/api/acl/test/")).andExpect(status().isOk());
    }

    @WithMockUser(authorities = ['acl:*:*'])
    void "wildcard check 2"() {
        expect:
        this.mockMvc.perform(get("/api/acl/test/")).andExpect(status().isOk());
        this.mockMvc.perform(post("/api/acl/test/")).andExpect(status().isOk());
        this.mockMvc.perform(put("/api/acl/test/")).andExpect(status().isOk());
        this.mockMvc.perform(delete("/api/acl/test/")).andExpect(status().isOk());
    }
}
