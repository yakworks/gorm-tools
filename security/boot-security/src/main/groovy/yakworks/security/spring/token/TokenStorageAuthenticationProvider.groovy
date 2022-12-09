package yakworks.security.spring.token

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken

/**
 * Adds AuthenticationProvider to the chain
 * This looks up the token in the db and authenticates if its good.
 */
class TokenStorageAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // BearerTokenAuthenticationToken bearer = (BearerTokenAuthenticationToken) authentication;
        // Jwt jwt = getJwt(bearer);
        // AbstractAuthenticationToken token = this.jwtAuthenticationConverter.convert(jwt);
        // token.setDetails(bearer.getDetails());
        // this.logger.debug("Authenticated token");

        throw new CredentialsExpiredException("Storage token")

        // return null
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
