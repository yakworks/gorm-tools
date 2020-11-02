/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.client

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.slurpersupport.GPathResult
import groovy.xml.StreamingMarkupBuilder

import org.grails.web.json.JSONElement
import org.springframework.http.ResponseEntity

import grails.converters.JSON
import grails.converters.XML

/**
 * Wraps a {@link ResponseEntity} allowing easy access to the underlying JSON or XML response. All methods of {@link ResponseEntity} are available on this class
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@SuppressWarnings(['Indentation']) //the lazy annotation makes this blow up in codenarc 1.3
@CompileStatic
class RestResponse {

    @Delegate
    ResponseEntity responseEntity

    String encoding = "UTF-8"

    RestResponse(ResponseEntity responseEntity) {
        this.responseEntity = responseEntity
    }

    @Lazy
    JSONElement json = {
        def body = responseEntity.body
        if(body instanceof JSONElement) {
            return (JSONElement)body
        }
        else if (body) {
            return (JSONElement)JSON.parse(body.toString())
        }
    }()

    @Lazy
    GPathResult xml = {
        def body = responseEntity.body
        if(body instanceof GPathResult) {
            return (GPathResult)body
        }
        else if (body) {
            return (GPathResult)XML.parse(body.toString())
        }
    }()

    @Lazy
    String text = {
        def body = responseEntity.body
        if( body instanceof GPathResult ) {
            return convertGPathResultToString(body)
        }
        else if (body) {
            return body.toString()
        }
        else {
            return responseEntity.statusCode.reasonPhrase
        }
    }()

    @CompileDynamic
    protected String convertGPathResultToString(Object body) {
        return new StreamingMarkupBuilder().bind {
            out << body
        }.toString()
    }

    int getStatus() {
        responseEntity?.statusCode?.value() ?: 200
    }
}
