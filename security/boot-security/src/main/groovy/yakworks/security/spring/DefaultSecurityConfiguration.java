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
import yakworks.security.spring.token.*;
import yakworks.security.spring.token.generator.JwtTokenGenerator;
import yakworks.security.spring.token.generator.OpaqueTokenGenerator;
import yakworks.security.spring.token.generator.StoreTokenGenerator;
import yakworks.security.spring.token.store.OpaqueTokenStoreAuthProvider;
import yakworks.security.spring.token.store.TokenStore;
import yakworks.security.spring.user.AuthSuccessUserInfoListener;
import yakworks.security.user.CurrentUser;
import yakworks.security.user.CurrentUserHolder;
import yakworks.util.StringUtils;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.ForwardAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration @Lazy
@Import({JwtConfiguration.class})
public class DefaultSecurityConfiguration {

    //@Autowired(required = false) TokenStore tokenStore;

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
        //addJsonAuthenticationFilter(http, tokenStore);
    }

    /** Example for simple Saml setup. Its largely dealt with in the configuration. */
    public static void applySamlSecurity(HttpSecurity http, AuthenticationSuccessHandler successHandler, String frontendCallbackUrl) throws Exception {

        http.saml2Login(saml2 -> {
                //saml2.defaultSuccessUrl("/saml", true);
                //saml2.defaultSuccessUrl("/api/token/callback", true);
                // saml2.loginProcessingUrl("{baseUrl}/api/login/saml2/sso/{registrationId}");
                saml2.successHandler(successHandler)
                    .defaultSuccessUrl(frontendCallbackUrl);
                // if(!StringUtils.hasLength(frontendCallbackUrl)){
                //     saml2.defaultSuccessUrl(frontendCallbackUrl);
                // }
            })
            .saml2Logout(Customizer.withDefaults());
    }

    /**
     * Legacy, Helper to setup a Filter to pick up POST to /api/login to it can be a REST call instead of just form post.
     */
    public static void addJsonAuthenticationFilter(HttpSecurity http, TokenStore tokenStore) throws Exception {
        //get the 2 beans needed
        ApplicationContext ctx = http.getSharedObject(ApplicationContext.class);

        //POC for enabling the legacy login with a POST to the /api/login endpoint.ObjMapper for parsing the json in the POST
        JsonUsernamePasswordLoginFilter jsonUnameFilter = new JsonUsernamePasswordLoginFilter(ctx.getBean(ObjectMapper.class));
        jsonUnameFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/login", "POST"));

        //forward over to the token endpoint which will return the standard bearer object.
        jsonUnameFilter.setAuthenticationSuccessHandler(new ForwardAuthenticationSuccessHandler("/tokenLegacy"));
        jsonUnameFilter.setAuthenticationManager(ctx.getBean(AuthenticationManager.class));
        http.addFilterAfter(jsonUnameFilter, BasicAuthenticationFilter.class);

        //adds the OpaqueTokenStoreAuthProvider that will look for Bearer tokens that start with opq_ prefix
        // will look them up in tokenStore (DB).
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(new OpaqueTokenStoreAuthProvider(tokenStore));
    }

    /** Sets up the JWT */
    public static void applyOauthJwt(HttpSecurity http) throws Exception {
        // JwtIssuerAuthenticationManagerResolver authenticationManagerResolver = new JwtIssuerAuthenticationManagerResolver
        //     ("https://idp.example.org/issuerOne", "https://idp.example.org/issuerTwo");
        //JWT
        // http.csrf((csrf) -> csrf.ignoringAntMatchers("/token"))
        http.csrf().disable();
        http.oauth2ResourceServer((oauth2) -> {
            oauth2.jwt();
            // oauth2.authenticationManagerResolver(authenticationManagerResolver);
        });
        // .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // .exceptionHandling((exceptions) -> exceptions
        // 		.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
        // 		.accessDeniedHandler(new BearerTokenAccessDeniedHandler())
        // );
    }

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
     * Success handler that adds cookie for token
     */
    @Bean
    CookieAuthSuccessHandler cookieSuccessHandler(JwtTokenGenerator tokenGenerator){
        CookieAuthSuccessHandler handler = new CookieAuthSuccessHandler();
        handler.setTokenGenerator(tokenGenerator);
        handler.setDefaultTargetUrl("/");
        // handler.setAlwaysUseDefaultTargetUrl(true);
        return handler;
    }

    /**
     * gets injected into the the BearerTokenAuthenticationFilter. Will look for cookie as well.
     */
    @Bean
    CookieBearerTokenResolver bearerTokenResolver(){
        return new CookieBearerTokenResolver();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        //this gets the default authManager from the the authConfig which gets injected during the autoconfig securityFilterChain process
        return authConfig.getAuthenticationManager();
    }

    @Bean
    @Lazy(false) //make sure its done on startup
    public AuthSuccessUserInfoListener authSuccessUserInfoListener() {
        return new AuthSuccessUserInfoListener();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecService secService() {
        return new SpringSecService();
    }

    //here just to set the static, never injected so make sure its not Lazy
    @Bean("${CurrentUserHolder.name}")
    @Lazy(false) //make sure its done on startup so it can be used
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

    @Bean
    @ConditionalOnMissingBean
    public OpaqueTokenGenerator opaqueTokenGenerator() {
        return new OpaqueTokenGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public StoreTokenGenerator storeTokenGenerator() {
        return new StoreTokenGenerator();
    }
}
