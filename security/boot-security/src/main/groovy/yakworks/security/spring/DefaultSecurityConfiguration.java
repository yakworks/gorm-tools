package yakworks.security.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import yakworks.security.SecService;
import yakworks.security.services.PasswordValidator;
import yakworks.security.spring.token.JwtTokenGenerator;
import yakworks.security.spring.token.TokenController;
import yakworks.security.spring.user.AuthSuccessUserInfoListener;
import yakworks.security.user.CurrentUser;
import yakworks.security.user.CurrentUserHolder;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Role;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.ForwardAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration @Lazy
@EnableConfigurationProperties({JwtProperties.class})
public class DefaultSecurityConfiguration {

    /**
     * Default securityFilterChain. Helper to set up HttpSecurity builder with default requestMatchers and forms.
     * NOTE: this is more of an example and a common simple setup for smoke test apps to use.
     * In your production app you would set this up and replace for its specifc security needs
     */
    @Bean
    @ConditionalOnMissingBean({SecurityFilterChain.class})
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        applyBasicDefaults(http);
        return http.build();
    }

    /**
     * Helper to set up HttpSecurity builder with default requestMatchers and forms.
     * NOTE: this is more of an example and common place for smoke test apps to use.
     * In your production app you would set this up for its specifc security needs
     */
    public static void applyBasicDefaults(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((authorize) -> authorize
            //by default this turns on anonymous access.
            .requestMatchers("/**").permitAll()
            //.requestMatchers("/actuator/**", "/resources/**", "/about").permitAll()
            .anyRequest().authenticated()
        )
        .httpBasic(withDefaults())
        .formLogin(withDefaults());
        // .formLogin( formLoginCustomizer ->
        //     formLoginCustomizer.defaultSuccessUrl("/", true)
        // )

        //add the Filter to pick up RESTful POST calls to /api/login credential passed in the body
        addJsonAuthenticationFilter(http);
    }

    /** Example for simple Saml setup. Its largely dealt with in the configuration. */
    public static void applySamlSecurity(HttpSecurity http) throws Exception {
        http.saml2Login(Customizer.withDefaults())
            .saml2Logout(Customizer.withDefaults());
    }

    /**
     * Legacy, Helper to setup a Filter to pick up POST to /api/login to it can be a REST call instead of just form post.
     */
    public static void addJsonAuthenticationFilter(HttpSecurity http) throws Exception {
        //get the 2 beans needed
        ApplicationContext ctx = http.getSharedObject(ApplicationContext.class);

        //POC for enabling the legacy login with a POST to the /api/login endpoint.ObjMapper for parsing the json in the POST
        JsonUsernamePasswordLoginFilter jsonUnameFilter = new JsonUsernamePasswordLoginFilter(ctx.getBean(ObjectMapper.class));
        jsonUnameFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/api/login", "POST"));
        //forward over to the token endpoint which will return the standard bearer object.
        jsonUnameFilter.setAuthenticationSuccessHandler(new ForwardAuthenticationSuccessHandler("/token"));
        jsonUnameFilter.setAuthenticationManager(ctx.getBean(AuthenticationManager.class));
        http.addFilterAfter(jsonUnameFilter, BasicAuthenticationFilter.class);
    }

    /** Sets up the JWT */
    public static void applyOauthJwt(HttpSecurity http) throws Exception {
        //JWT
        // http.csrf((csrf) -> csrf.ignoringAntMatchers("/token"))
        http.csrf().disable();
        http.oauth2ResourceServer((oauthServer) ->
            oauthServer.jwt()
        );
        // .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // .exceptionHandling((exceptions) -> exceptions
        // 		.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
        // 		.accessDeniedHandler(new BearerTokenAccessDeniedHandler())
        // );
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        //this gets the default authManager from the the authConfig which gets injected during the autoconfig securityFilterChain process
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public AuthSuccessUserInfoListener authSuccessUserInfoListener() {
        return new AuthSuccessUserInfoListener();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecService secService() {
        return new SpringSecService();
    }

    //here just to set the static, never injected so make sure its not Lazy
    @Bean("${CurrentUserHolder.name}") @Lazy(false)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public CurrentUserHolder CurrentUserHolder() {
        //here just to set the static, there a better way?
        return new CurrentUserHolder();
    }

    @Bean
    @ConditionalOnMissingBean
    public CurrentUser currentUser() {
        return new CurrentSpringUser();
    }

    @Bean
    @ConditionalOnMissingBean
    public PasswordValidator passwordValidator() {
        return new PasswordValidator();
    }

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Configuration
    @Lazy
    @ConditionalOnClass(JwtDecoder.class)
    public static class JwtTokenConfiguration {

        @Bean @ConditionalOnMissingBean
        public TokenController tokenController() {
            return new TokenController();
        }

        @Bean @ConditionalOnMissingBean
        @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
        public KeyPair rsaKeyPair(JwtProperties jwtProperties) {
            return new KeyPair(jwtProperties.getPublicKey(), jwtProperties.getPrivateKey());
        }

        @Bean @ConditionalOnMissingBean
        public JwtDecoder jwtDecoder(KeyPair rsaKeyPair) {
            return NimbusJwtDecoder.withPublicKey((RSAPublicKey) rsaKeyPair.getPublic()).build();
        }

        @Bean
        @ConditionalOnMissingBean
        public JwtEncoder jwtEncoder(KeyPair rsaKeyPair) {
            JWK jwk = new RSAKey.Builder((RSAPublicKey) rsaKeyPair.getPublic()).privateKey((RSAPrivateKey) rsaKeyPair.getPrivate()).build();
            JWKSource<SecurityContext> jwks = new ImmutableJWKSet<SecurityContext>(new JWKSet(jwk));
            NimbusJwtEncoder encoder = new NimbusJwtEncoder(jwks);
            return encoder;
        }

        @Bean
        @ConditionalOnMissingBean
        public JwtTokenGenerator tokenGenerator() {
            return new JwtTokenGenerator();
        }

    }
}
