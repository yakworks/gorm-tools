/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.rest.token

import javax.annotation.PostConstruct
import javax.annotation.Resource

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService

import gorm.tools.idgen.IdGenerator
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.rest.token.storage.TokenNotFoundException
import grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import yakworks.security.gorm.model.AppUserToken

/**
 * -- Copied in from grails rest-scurity as a starting point --
 * We should not be storing the token as clear text in the database.
 * While they might be short lived they are still a password.
 * we can use Postgres crypt to make look ups simple
 *
 * Once the username is found, it will delegate to the configured {@link UserDetailsService} for obtaining authorities
 * information.
 *
 * openssl genrsa -out private.pem 1024
 * openssl rsa -in private.pem -pubout -out public.pem
 * see this to build in RSA https://github.com/grails-plugins/grails-spring-security-rest/pull/315/files
 */
@Slf4j
@CompileStatic
class PostgresTokenStorageService implements TokenStorageService {

    @Autowired UserDetailsService userDetailsService

    @Autowired JdbcTemplate jdbcTemplate

    @Resource(name="idGenerator")
    IdGenerator idGenerator

    //default for how long from creation token is valid
    public static String defaultExpires = '8h'

    @PostConstruct
    void init(){
        log.debug "make sure the crypto extension is enabled with CREATE EXTENSION IF NOT EXISTS pgcrypto"
        jdbcTemplate.execute("""
            CREATE EXTENSION IF NOT EXISTS pgcrypto
        """)
    }

    UserDetails loadUserByToken(String tokenValue) throws TokenNotFoundException {
        log.debug "Finding token ${tokenValue} in GORM"
        String username = findUsernameForExistingToken(tokenValue)

        if (username) {
            return userDetailsService.loadUserByUsername(username)
        }

        throw new TokenNotFoundException("Token ${tokenValue} not found")
    }

    @Transactional
    void storeToken(String tokenValue, UserDetails principal) {
        // log.debug "Storing principal for token: ${tokenValue}"
        log.debug "Principal: ${principal}"

        // AppUserToken newTokenObject = new AppUserToken(tokenValue: tokenValue, username: principal.username)
        // newTokenObject.save()
        Long id = idGenerator.getNextId('AppUserToken.id')
        jdbcTemplate.update("""
            insert into AppUserToken (id, username, tokenvalue,
                expiredate,
                createdDate, createdBy, editedDate, editedBy)
            values (${id}, '${principal.username}', crypt('${tokenValue}',gen_salt('md5')),
                    now() + interval '${defaultExpires}',
                    now(), 1, now(), 1 )
        """)

    }

    @Transactional
    void removeToken(String tokenValue) throws TokenNotFoundException {
        log.debug "Removing token ${tokenValue} from GORM"
        String username = findUsernameForExistingToken(tokenValue)
        if (username) {
            AppUserToken.findWhere(username: username).remove()
        } else {
            throw new TokenNotFoundException("Token ${tokenValue} not found")
        }

    }

    @SuppressWarnings('ReturnNullFromCatchBlock')
    @Transactional
    String findUsernameForExistingToken(String tokenValue) {
        log.debug "Searching in GORM for UserDetails of token ${tokenValue}"
        try {
            return jdbcTemplate.queryForObject("""\
                SELECT username
                FROM appusertoken apt
                WHERE (tokenvalue = crypt('${tokenValue}', tokenvalue)) = true
                    AND expiredate > now();
            """, String)
        } catch(e){
            return null
        }
    }

}
