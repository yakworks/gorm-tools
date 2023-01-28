/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.client


import java.nio.charset.StandardCharsets
import java.time.Duration

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriUtils

import reactor.netty.http.client.HttpClient
import yakworks.commons.lang.EnumUtils
import yakworks.json.groovy.JsonEngine

import static org.springframework.web.reactive.function.client.WebClient.Builder
import static org.springframework.web.reactive.function.client.WebClient.ResponseSpec

/**
 * Trait with helper methods to wrap OKHttps HttpClient for rest api testing
 */
@CompileStatic
//@CompileDynamic
trait WebClientTrait {

    @Autowired Builder wbuilder

    String jsonHeader = "application/json;charset=utf-8"

    @Value('${spring.security.user.name:admin}')
    String username

    @Value('${spring.security.user.password:123}')
    String password

    /**
     * Main method to fire a request and get a response.
     * builds request and executes it with the OkHttpclient.
     * @param method - POST, PUT, GET, DELETE, PATCH, etc..
     * @param uriPath - the path, will call getUrl to append baseUrl if doesnt start with http
     * @param body - the body object to convert to json, usually a Map or sometimes List
     * @return the ResponseEntity
     */
    ResponseEntity<Map> execute(HttpMethod method, String uriPath){
        ResponseEntity respEnt = headerSpec(method, uriPath)
            .retrieve().toEntity(Map).block()

        return respEnt
    }

    /**
     * gets HttpMethod enum from string
     * @param name GET, POST, PUT, etc..
     * @return the HttpMethod enum
     */
    HttpMethod getMethod(String name){
        return EnumUtils.getEnum(HttpMethod, name.toUpperCase())
    }

    ResponseEntity<Map> execute(HttpMethod method, String uriPath, Object body){
        ResponseEntity respEnt  = headerSpec(method, uriPath)
            .bodyValue(body)
            .retrieve()
            .toEntity(Map).block();

        return respEnt
    }

    ResponseEntity<byte[]> executeBytes(HttpMethod method, String uriPath){
        ResponseEntity respEnt  = headerSpec(method, uriPath)
            .retrieve()
            .toEntity(byte[].class).block()

        return respEnt
    }

    <T> T doMethod(HttpMethod method, String uriPath, Class<T> clazz = Map){
        T respEnt  = retrieve(method, uriPath)
            .bodyToMono(clazz)
            .block()

        return respEnt
    }

    ResponseSpec retrieve(HttpMethod method, String uriPath){
        return headerSpec(method, uriPath).retrieve()
    }

    WebClient.RequestBodySpec headerSpec(HttpMethod method, String uriPath){
        WebClient.RequestBodySpec  reqSpec  = webClient
            .method(method)
            .uri(URI.create("${getBaseUrl()}${uriPath}"))
            .header("Authorization", "Bearer ${OkAuth.TOKEN}")

        return reqSpec
    }

    /**
     * build WebClient with higher timeout of 120
     */
    WebClient getWebClient(){
        HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(120))
        WebClient webClient = wbuilder
            .baseUrl(getBaseUrl())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build()
        return webClient
    }

    /**
     * build WebClient with higher timeout of 120
     */
    // WebClient getWebClientJava11(){
        // HttpClient httpClient = HttpClient.create()
        //     .responseTimeout(Duration.ofSeconds(120))

    //     HttpClient httpClient = newBuilder()
    //         .followRedirects(Redirect.NORMAL)
    //         .connectTimeout(Duration.ofSeconds(20))
    //         .build();
    //
    //     ClientHttpConnector connector =
    //         new JdkClientHttpConnector(httpClient, new DefaultDataBufferFactory());
    //
    //     WebClient webClient = wbuilder
    //         .baseUrl(getBaseUrl())
    //         .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    //         .clientConnector(connector)
    //         .build()
    //     //increase timeout to 120 from 10 so we can debug without socketTimeout
    //     return webClient
    // }

    //POST test
    ResponseEntity<Map> post(String uriPath, Object body) {
        return execute(HttpMethod.POST, uriPath, body)
    }

    //PUT
    ResponseEntity<Map> put(String uriPath, Map body, Object id) {
        put("$uriPath/$id", body)
    }

    ResponseEntity<Map> put(String uriPath, Object body) {
        return execute(HttpMethod.PUT, uriPath, body)
    }

    // ResponseEntity<Map> get(String uriPath, Object id) {
    //     return get("$uriPath/$id")
    // }

    ResponseEntity<Map> get(String uriPath) {
        return execute(HttpMethod.GET, uriPath)
    }

    ResponseEntity<byte[]> getBytes(String uriPath) {
        return executeBytes(HttpMethod.GET, uriPath)
    }

    <T> T getBody(String uriPath, Class<T> clazz = Map) {
        return doMethod(HttpMethod.GET, uriPath)
    }

    ResponseEntity<Map> delete(String uriPath, Object id) {
        delete("$uriPath/$id")
    }

    ResponseEntity<Map> delete(String uriPath) {
        return execute(HttpMethod.DELETE, uriPath)
    }

    /**
     * login with @Value injected username and password if not already
     */
    String login() {
        return OkAuth.TOKEN ?: login(getUsername(), getPassword())
    }

    @CompileDynamic
    /** uses the basic auth to login and parse the access_token from response. */
    String login(String uname, String pwd) {

        Map resp = webClient
            .post()
            .uri("/api/oauth/token")
            .headers(h -> h.setBasicAuth(uname, pwd))
            .retrieve().bodyToMono(Map).block();

        OkAuth.TOKEN = resp.access_token
        return resp.access_token as String
    }

    @CompileDynamic
    String getBaseUrl(){
        //serverPort is provided by the test, spring auto assigns it.
        return "http://localhost:$serverPort"
    }

    String getUrl(String uriPath){
        return uriPath.startsWith('http') ? uriPath : "${getBaseUrl()}$uriPath"
    }

    String encodeQueryParam(String val){
        return UriUtils.encodeQueryParam(val, StandardCharsets.UTF_8)
    }

    /**
     * convert the body onject to json strino
     */
    String toJson(Object body){
        JsonEngine.toJson(body)
    }
}
