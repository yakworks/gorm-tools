package yakworks.security.spring;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import com.nimbusds.jwt.proc.JWTProcessor;
import yakworks.security.spring.token.JwtProperties;
import yakworks.security.spring.token.TokenController;
import yakworks.security.spring.token.generator.JwtTokenExchanger;
import yakworks.security.spring.token.generator.JwtTokenGenerator;

import java.security.KeyPair;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;

@Configuration @Lazy
@ConditionalOnClass(OAuth2Token.class)
@EnableConfigurationProperties({JwtProperties.class})
public class JwtConfiguration {

    @Bean @ConditionalOnMissingBean
    public TokenController tokenController() {
        return new TokenController();
    }


    //TODO
    @Bean
    @ConditionalOnMissingBean
    JWTClaimsSetAwareJWSKeySelector claimsSetKeySelector( JwtProperties jwtProperties) {
        // JWSKeySelector<SecurityContext> jwsKeySelector = new SingleKeyJWSKeySelector<>(JWSAlgorithm.RS256, rsaKeyPair.getPublic());
        // return jwsKeySelector;
        //var rsaKeyPair = rsaKeyPairMap.values().iterator().next();
        return (JWSHeader header, JWTClaimsSet claimsSet, SecurityContext context) -> {
            Collection<JwtProperties.Issuer> issuers = jwtProperties.getIssuers().values();
            var iss = claimsSet.getIssuer();
            if(iss != null) {
                var issConfig= issuers.stream()
                    .filter(issuer -> iss.equals(issuer.getIss()))
                    .findAny()
                    .orElse(null);
                // var pub = jwtProperties.getMap();
                return Collections.singletonList(issConfig.getKeyPair().getPublic());
            }
            //return the default if not found
            //FIXME get rid of this
            return Collections.singletonList(jwtProperties.getDefaultIssuer().getKeyPair().getPublic());
        };
    }

    @Bean
    JWTProcessor jwtProcessor(JWTClaimsSetAwareJWSKeySelector claimsSetKeySelector) {
        DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWTClaimsSetAwareJWSKeySelector(claimsSetKeySelector);
        // Spring Security validates the claim set independent from Nimbus
        // jwtProcessor.setJWTClaimsSetVerifier((claims, context) -> {});
        return jwtProcessor;
    }

    @Bean
    JwtDecoder jwtDecoder(JWTProcessor jwtProcessor) {
        NimbusJwtDecoder decoder = new NimbusJwtDecoder(jwtProcessor);
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefault());
        decoder.setJwtValidator(validator);
        return decoder;
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtEncoder jwtEncoder(JwtProperties jwtProperties) {
        JwtProperties.Issuer issuer = jwtProperties.getDefaultIssuer();
        KeyPair keyPair = issuer.getKeyPair();
        JWK jwk;
        if(issuer.isEC()){
            jwk = new ECKey.Builder(Curve.P_256, (ECPublicKey) keyPair.getPublic())
                .privateKey(keyPair.getPrivate()).build();
        } else {
            jwk = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate()).build();
        }

        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<SecurityContext>(new JWKSet(jwk));
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(jwks);
        return encoder;
    }

    // WIP For EC256
    // @Bean
    // @ConditionalOnMissingBean
    // public JwtEncoder jwtEncoder(JwtProperties jwtProperties) {
    //     KeyPair keyPair = jwtProperties.getDefaultIssuer().getKeyPair();
    //     JWK jwk = new ECKey.Builder(Curve.P_256, (ECPublicKey) keyPair.getPublic())
    //         .privateKey(keyPair.getPrivate()).build();
    //     JWKSource<SecurityContext> jwks = new ImmutableJWKSet<SecurityContext>(new JWKSet(jwk));
    //     NimbusJwtEncoder encoder = new NimbusJwtEncoder(jwks);
    //     return encoder;
    // }

    @Bean
    @ConditionalOnMissingBean
    public JwtTokenGenerator tokenGenerator() {
        return new JwtTokenGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtTokenExchanger jwtTokenExchanger() {
        return new JwtTokenExchanger();
    }

    // @Bean
    // @ConditionalOnMissingBean
    // @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    // public KeyPair rsaKeyPair(JwtProperties jwtProperties) {
    //     return new KeyPair(jwtProperties.getPublicKey(), jwtProperties.getPrivateKey());
    // }

    // // the default simple way.
    // @Bean @ConditionalOnMissingBean
    // public JwtDecoder jwtDecoder(KeyPair rsaKeyPair) {
    //     return NimbusJwtDecoder.withPublicKey((RSAPublicKey) rsaKeyPair.getPublic()).build();
    // }
}
