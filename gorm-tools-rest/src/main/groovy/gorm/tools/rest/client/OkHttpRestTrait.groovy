/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.client

import groovy.json.JsonSlurper
import groovy.json.StreamingJsonBuilder
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import gorm.tools.beans.Maps
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

/**
 * Trait with helper methods to wrap OKHttps HttpClient for rest api testing
 */
@CompileStatic
//@CompileDynamic
trait OkHttpRestTrait {

    String jsonHeader = "application/json;charset=utf-8"

    OkHttpClient getHttpClient(){
        new OkHttpClient()
    }

    @CompileDynamic
    String getBaseUrl(){
        return "http://localhost:$serverPort"
    }

    String getUrl(String uriPath){
        return uriPath.startsWith('http') ? uriPath : "${getBaseUrl()}$uriPath"
    }

    MediaType getJsonMediaType(){
        MediaType.parse(jsonHeader)
    }

    Response post(String uriPath, Map body) {
        String jsonBody = toJson(body)
        RequestBody requestBody = RequestBody.create(jsonBody, getJsonMediaType())

        Request request = new Request.Builder().url(getUrl(uriPath))
            .addHeader("Content-Type", jsonHeader)
            .post(requestBody)
            .build()

        return getHttpClient().newCall(request).execute()
    }

    Response put(String uriPath, Map body, Object id) {
        put("$uriPath/$id", body)
    }

    Response put(String uriPath, Map body) {
        String jsonBody = toJson(body)
        RequestBody requestBody = RequestBody.create(jsonBody, getJsonMediaType())

        Request request = new Request.Builder().url(getUrl(uriPath))
            .addHeader("Content-Type", jsonHeader)
            .put(requestBody)
            .build()

        return getHttpClient().newCall(request).execute()
    }

    Response get(String uriPath, Object id) {
        return get("$uriPath/$id")
    }

    Response get(String uriPath) {
        Request request = new Request.Builder().url(getUrl(uriPath))
            .get()
            .build()

        return getHttpClient().newCall(request).execute()
    }

    Response delete(String uriPath, Object id) {
        delete("$uriPath/$id")
    }

    Response delete(String uriPath) {
        Request request = new Request.Builder().url(getUrl(uriPath))
            .delete()
            .build()

        return getHttpClient().newCall(request).execute()
    }

    Map bodyToMap(Response resp){
        new JsonSlurper().parseText(resp.body().string()) as Map
    }

    List bodyToList(Response resp){
        new JsonSlurper().parseText(resp.body().string()) as List
    }

    String toJson(Map body){
        def w = new StringWriter()
        def sjb = new StreamingJsonBuilder(w)
        sjb.call(body)
        return w.toString()
    }

    String toJson(List body){
        def w = new StringWriter()
        def sjb = new StreamingJsonBuilder(w)
        sjb.call(body)
        return w.toString()
    }
}
