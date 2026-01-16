/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.client

import java.util.concurrent.TimeUnit

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.jetbrains.annotations.NotNull
import org.springframework.beans.factory.annotation.Value

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Credentials
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.internal.http.HttpMethod
import yakworks.json.groovy.JsonEngine

/**
 * Trait with helper methods to wrap OKHttps HttpClient for rest api testing
 */
@CompileStatic
trait OkHttpRestTrait {

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
     * @return the okhttp response
     */
    Response execute(String method, String uriPath, Object body = null){
        Request request = getRequestBuilder(uriPath)
            .method(method, getRequestBody(method, body))
            .build()
        return getHttpClient().newCall(request).execute()
    }

    void enqueue(String method, String uriPath, Object body = null){
        Request request = getRequestBuilder(uriPath)
            .method(method, getRequestBody(method, body))
            .build()

        getHttpClient().newCall(request).enqueue(
            new EmptyCallBack()
        );
    }

    static class EmptyCallBack implements Callback{
        @Override
        void onFailure(@NotNull Call call, @NotNull IOException e) {
        }
        @Override
        void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        }
    }
    /**
     * build and OkHttpClient with higher time out of 120
     */
    OkHttpClient getHttpClient(){
        //increase timeout to 120 from 10 so we can debug without socketTimeout
        new OkHttpClient.Builder().readTimeout(120, TimeUnit.SECONDS).build()
    }

    /**
     * converts body object to json and creates a RequestBody
     */
    RequestBody getRequestBody(String method, Object body){
        //do some shenanagans so that we return empty body when POST or PUT
        if(body == null && HttpMethod.requiresRequestBody(method)){
            return RequestBody.create("", null)
        } else if(body == null){
            //on GET and DELETE no body is allowed.
            return null
        } else {
            String jsonBody = toJson(body)
            return RequestBody.create(jsonBody, MediaType.parse(jsonHeader))
        }
    }

    /**
     * creates a Request Builder with content-type header set
     * and Authorization header if field is set
     */
    Request.Builder getRequestBuilder(String uriPath){
        Request.Builder bldr = new Request.Builder()
            .url(getUrl(uriPath))
            .addHeader("Content-Type", jsonHeader)

        if(OkAuth.TOKEN) bldr.addHeader("Authorization", "Bearer ${OkAuth.TOKEN}")

        return bldr
    }

    @CompileDynamic
    String getBaseUrl(){
        //serverPort is provided by the test, spring auto assigns it.
        return "http://localhost:$serverPort"
    }

    String getUrl(String uriPath){
        return uriPath.startsWith('http') ? uriPath : "${getBaseUrl()}$uriPath"
    }

    MediaType getJsonMediaType(){
        MediaType.parse(jsonHeader)
    }

    //POST test
    Response post(String uriPath, Object body) {
        return execute("POST", uriPath, body)
    }

    //PUT
    Response put(String uriPath, Map body, Object id) {
        put("$uriPath/$id", body)
    }

    Response put(String uriPath, Object body) {
        return execute("PUT", uriPath, body)
    }

    Response get(String uriPath, Object id) {
        return get("$uriPath/$id")
    }

    Response get(String uriPath) {
        return execute("GET", uriPath)
    }

    Response delete(String uriPath, Object id) {
        delete("$uriPath/$id")
    }

    Response delete(String uriPath) {
        return execute("DELETE", uriPath)
    }

    /**
     * login with @Value injected username and password if not already
     */
    String login() {
        if(!OkAuth.TOKEN) login(getUsername(), getPassword())
        return OkAuth.TOKEN
    }

    //can override to change the path for getting the oauth token
    String getLoginTokenPath(){
        "api/oauth/token"
    }

    @CompileDynamic
    /** uses the basic auth to login and parse the access_token from response. */
    String login(String uname, String pwd) {
        //create the basic auth credentials
        String basicAuth = Credentials.basic(uname, pwd)
        String lpath = "http://localhost:${serverPort}/${getLoginTokenPath()}"
        Request request = new Request.Builder()
            .url(lpath)
            .addHeader("Authorization", basicAuth)
            .addHeader("Content-Type", jsonHeader)
            .method("POST", RequestBody.create("", null))
            .build()

        Response resp = getHttpClient().newCall(request).execute()
        assert resp.successful
        Map body = bodyToMap(resp)
        OkAuth.TOKEN = body.access_token
        return body.access_token as String
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
    String toJson(Object body, boolean removeNullsKeys = true){
        JsonEngine.toJson(body, removeNullsKeys)
    }
}
