/*
 * Copyright 2020 the original author or authors.
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

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Lazy
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

import yakworks.openapi.gorm.OpenApiGenerator
import yakworks.rally.RallyConfiguration
import yakworks.rest.grails.AppInfoBuilder
import yakworks.security.spring.DefaultSecurityConfiguration
import yakworks.security.spring.token.CookieAuthSuccessHandler
import yakworks.security.spring.token.CookieUrlTokenSuccessHandler
import yakworks.security.spring.token.TokenUtils
import yakworks.security.spring.token.generator.JwtTokenGenerator
import yakworks.security.spring.token.store.TokenStore

import static org.springframework.security.config.Customizer.withDefaults

/**
 * An example of explicitly configuring Spring Security with the defaults.
 */
// keep componentScan in Application.groovy for now so unit test work. see notes in the TestSpringApplication class in tests
// @ComponentScan(['yakity.security', 'yakworks.security'])
@Lazy
@EnableWebSecurity //(debug = true)
@CompileStatic
@Configuration
@Import([RallyConfiguration])
class RallyApiSpringConfig {

    @Bean
    AppInfoBuilder appInfoBuilder() {
        return new AppInfoBuilder()
    }
}
