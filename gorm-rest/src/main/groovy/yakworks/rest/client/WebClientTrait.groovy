/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.client


import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.concurrent.TimeUnit

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

import okhttp3.OkHttpClient
import okhttp3.Response
import reactor.netty.http.client.HttpClient
import yakworks.commons.lang.EnumUtils
import yakworks.json.groovy.JsonEngine

/**
 * Trait with helper methods to wrap OKHttps HttpClient for rest api testing
 */
@CompileStatic
//@CompileDynamic
trait WebClientTrait {

    @Autowired WebClient.Builder wbuilder

    String jsonHeader = "application/json;charset=utf-8"

    @Value('${spring.security.user.name:admin}')
    String username

    @Value('${spring.security.user.password:123}')
    String password

    String encodeQueryParam(String val){
        return UriUtils.encodeQueryParam(val, StandardCharsets.UTF_8)
    }

    /**
     * Main method to fire a request and get a response.
     * builds request and executes it with the OkHttpclient.
     * @param method - POST, PUT, GET, DELETE, PATCH, etc..
     * @param uriPath - the path, will call getUrl to append baseUrl if doesnt start with http
     * @param body - the body object to convert to json, usually a Map or sometimes List
     * @return the ResponseEntity
     */
    ResponseEntity<Map> execute(String method, String uriPath){
        ResponseEntity respEnt  = webClient
            .method(EnumUtils.getEnum(HttpMethod, method.toUpperCase()))
            .uri(URI.create("${getBaseUrl()}${uriPath}"))
            .header("Authorization", OkAuth.BEARER_TOKEN)
            .retrieve().toEntity(Map).block();

        return respEnt
    }

    ResponseEntity<Map> execute(String method, String uriPath, Object body){
        ResponseEntity respEnt  = webClient
            .method(EnumUtils.getEnum(HttpMethod, method.toUpperCase()))
            .uri(uriPath)
            .header("Authorization", OkAuth.BEARER_TOKEN)
            .bodyValue(body)
            .retrieve().toEntity(Map).block();

        return respEnt
    }

    ResponseEntity<byte[]> executeBytes(String method, String uriPath){
        ResponseEntity respEnt  = webClient
            .method(EnumUtils.getEnum(HttpMethod, method.toUpperCase()))
            .uri(URI.create("${getBaseUrl()}${uriPath}"))
            .header("Authorization", OkAuth.BEARER_TOKEN)
            .retrieve().toEntity(byte[].class).block();

        return respEnt
    }

    <T> T doMethod(String method, String uriPath, Class<T> clazz = Map){
        T respEnt  = webClient
            .method(EnumUtils.getEnum(HttpMethod, method.toUpperCase()))
            .uri(uriPath)
            .header("Authorization", OkAuth.BEARER_TOKEN)
            .retrieve()
            .bodyToMono(clazz).block();

        return respEnt
    }

    /**
     * build and OkHttpClient with higher time out of 120
     */
    WebClient getWebClient(){
        HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(120))
        WebClient webClient = wbuilder
            .baseUrl(getBaseUrl())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build()
        //increase timeout to 120 from 10 so we can debug without socketTimeout
        return webClient
    }

    @CompileDynamic
    String getBaseUrl(){
        //serverPort is provided by the test, spring auto assigns it.
        return "http://localhost:$serverPort"
    }

    String getUrl(String uriPath){
        return uriPath.startsWith('http') ? uriPath : "${getBaseUrl()}$uriPath"
    }

    //POST test
    ResponseEntity<Map> post(String uriPath, Object body) {
        return execute("POST", uriPath, body)
    }

    //PUT
    ResponseEntity<Map> put(String uriPath, Map body, Object id) {
        put("$uriPath/$id", body)
    }

    ResponseEntity<Map> put(String uriPath, Object body) {
        return execute("PUT", uriPath, body)
    }

    ResponseEntity<Map> get(String uriPath, Object id) {
        return get("$uriPath/$id")
    }

    ResponseEntity<Map> get(String uriPath) {
        return execute("GET", uriPath)
    }

    ResponseEntity<byte[]> getBytes(String uriPath) {
        return executeBytes("GET", uriPath)
    }

    <T> T getBody(String uriPath, Class<T> clazz = Map) {
        return doMethod("GET", uriPath)
    }

    ResponseEntity<Map> delete(String uriPath, Object id) {
        delete("$uriPath/$id")
    }

    ResponseEntity<Map> delete(String uriPath) {
        return execute("DELETE", uriPath)
    }

    /**
     * login with @Value injected username and password if not already
     */
    String login() {
        if(!OkAuth.BEARER_TOKEN) login(getUsername(), getPassword())
    }

    OkHttpClient getHttpClient(){
        //increase timeout to 120 from 10 so we can debug without socketTimeout
        new OkHttpClient.Builder().readTimeout(120, TimeUnit.SECONDS).build()
    }

    @CompileDynamic
    /** uses the basic auth to login and parse the access_token from response. */
    String login(String uname, String pwd) {

        Map resp = webClient
            .post()
            .uri("/api/token")
            .headers(h -> h.setBasicAuth(uname, pwd))
            .retrieve().bodyToMono(Map).block();

        OkAuth.TOKEN = resp.access_token
        OkAuth.BEARER_TOKEN = "Bearer ${resp.access_token}"
        return resp.access_token as String
    }

    /**
     * parse the body json to map
     * @param resp the okhttp reponse
     * @return the response body as map
     */
    Map bodyToMap(Response resp){
        JsonEngine.parseJson(resp.body().string(), Map)
    }

    /**
     * parse the body json to list
     */
    List bodyToList(Response resp){
        JsonEngine.parseJson(resp.body().string(), List)
    }

    /**
     * convert the body onject to json strino
     */
    String toJson(Object body){
        JsonEngine.toJson(body)
    }
}
