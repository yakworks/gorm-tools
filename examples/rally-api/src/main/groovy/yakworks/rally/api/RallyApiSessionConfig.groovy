/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api

import groovy.transform.CompileStatic

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.session.FlushMode
import org.springframework.session.SaveMode
import org.springframework.session.config.SessionRepositoryCustomizer
import org.springframework.session.hazelcast.Hazelcast4IndexedSessionRepository
import org.springframework.session.hazelcast.Hazelcast4PrincipalNameExtractor
import org.springframework.session.hazelcast.config.annotation.SpringSessionHazelcastInstance
import org.springframework.session.hazelcast.config.annotation.web.http.EnableHazelcastHttpSession
import org.springframework.session.web.http.CookieSerializer
import org.springframework.session.web.http.DefaultCookieSerializer

import com.hazelcast.config.AttributeConfig
import com.hazelcast.config.Config
import com.hazelcast.config.IndexConfig
import com.hazelcast.config.IndexType
import com.hazelcast.config.KubernetesConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance

// the component scan here does not seem to be the same as the packageNames and is needed to pick up the
// the services marked with @Component
// NOTE: @Lazy(false) to make sure Jobs are NOT Lazy, they need to be registered at init to get scheduled.
@Configuration
@EnableHazelcastHttpSession
@CompileStatic
@SuppressWarnings(['UnnecessaryDotClass'])
class RallyApiSessionConfig {
    final private static Logger log = LoggerFactory.getLogger(RallyApiSessionConfig)

    private final String HAZEL_MAP = "hazel-session-map-name";

    //mostly here for Okta Saml and to make setSameSite null instead of lax
    @Bean
    CookieSerializer cookieSerializer() {
        DefaultCookieSerializer defaultCookieSerializer = new DefaultCookieSerializer();
        defaultCookieSerializer.setCookiePath("/") //change from /api
        defaultCookieSerializer.setUseSecureCookie(true)
        //Lax by default so null it out, seems to be needed for Okta Saml redirect.
        defaultCookieSerializer.setSameSite(null)
        //defaultCookieSerializer.setCookieName("NSESSIONID"); //default is SESSION
        //defaultCookieSerializer.setUseHttpOnlyCookie(false);
        //defaultCookieSerializer.setDomainName("somedomain.com");
        return defaultCookieSerializer
    }

    // @Bean
    // MapSessionRepository sessionRepository() {
    //     return new MapSessionRepository(new ConcurrentHashMap<>());
    // }

    @Bean
    public SessionRepositoryCustomizer<Hazelcast4IndexedSessionRepository> customize() {
        return (Hazelcast4IndexedSessionRepository sessionRepository) -> {
            sessionRepository.setFlushMode(FlushMode.IMMEDIATE);
            sessionRepository.setSaveMode(SaveMode.ALWAYS);
            sessionRepository.setSessionMapName(HAZEL_MAP);
            sessionRepository.setDefaultMaxInactiveInterval(900);
        } as SessionRepositoryCustomizer<Hazelcast4IndexedSessionRepository>
    }

    @Bean
    @SpringSessionHazelcastInstance
    public HazelcastInstance hazelcastInstance() {
        Config config = new Config();
        config.setClusterName("spring-hazel-session-cluster");
        //[hz-member-1] is the name referenced from hibernate
        config.instanceName = "hz-member-1"
        //config.integrityCheckerConfig.enabled = true
        config.networkConfig.join.multicastConfig.enabled = false
        String k8sNamespace = System.getenv('KUBERNETES_NAMESPACE')
        if(k8sNamespace){
            String serviceName = System.getenv('APP_KEY')
            KubernetesConfig kubernetesConfig = config.networkConfig.join.kubernetesConfig
            kubernetesConfig.setEnabled(true).setProperty("namespace", k8sNamespace)
            if(serviceName){
                kubernetesConfig.setProperty("service-name", serviceName)
            }
            log.info(" ☎️ ☎️ ☎️ ----- Hazelcast K8S Config [ namespace: $k8sNamespace, serviceName: $serviceName ] ------  ☎️ ☎️ ☎️")
        }
        // Add this attribute to be able to query sessions by their PRINCIPAL_NAME_ATTRIBUTE's
        AttributeConfig attributeConfig = new AttributeConfig()
            .setName(Hazelcast4IndexedSessionRepository.PRINCIPAL_NAME_ATTRIBUTE)
            .setExtractorClassName(Hazelcast4PrincipalNameExtractor.class.getName());

        // Configure the sessions map
        config.getMapConfig(HAZEL_MAP)
            .addAttributeConfig(attributeConfig).addIndexConfig(
            new IndexConfig(IndexType.HASH, Hazelcast4IndexedSessionRepository.PRINCIPAL_NAME_ATTRIBUTE));

        // Use custom serializer to de/serialize sessions faster. This is optional.
        // Note that, all members in a cluster and connected clients need to use the
        // same serializer for sessions. For instance, clients cannot use this serializer
        // where members are not configured to do so.
        // SerializerConfig serializerConfig = new SerializerConfig();
        // serializerConfig.setImplementation(new HazelcastSessionSerializer()).setTypeClass(MapSession.class);
        // config.getSerializationConfig().addSerializerConfig(serializerConfig);

        return Hazelcast.newHazelcastInstance(config);
    }


}
