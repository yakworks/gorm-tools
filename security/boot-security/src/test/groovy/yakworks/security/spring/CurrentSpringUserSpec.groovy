package yakworks.security.spring

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import spock.lang.Specification

import yakworks.security.spring.user.SpringUser

class CurrentSpringUserSpec extends Specification {

    CurrentSpringUser currentUser = new CurrentSpringUser()

    void cleanup() {
        SecurityContextHolder.clearContext()
    }

    void "hasWildcardPermission"() {
        expect:
        login(perms)
        currentUser.hasWildcardPermission() == expected

        where:
        perms                          | expected
        ['*:*:*']                      | true
        ['*']                          | true
        ['*:*']                        | true
        ['printer:print:*']            | false
        ['*:*:read']                   | false
        ['rally:org:*']                | false
        ['printer:print:*', '*:*:*']   | true
        []                             | false
    }

    private void login(Collection<String> perms) {
        def user = SpringUser.create(username: 'admin', permissions: perms as Set, roles: ['ADMIN'] as Set)
        def auth = new UsernamePasswordAuthenticationToken(user, null, user.authorities)
        auth.details = user
        SecurityContextHolder.context.authentication = auth
    }
}
