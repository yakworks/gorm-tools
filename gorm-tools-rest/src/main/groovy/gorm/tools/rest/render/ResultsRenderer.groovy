/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.render

import groovy.json.StreamingJsonBuilder
import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.NoSuchMessageException

import gorm.tools.beans.Pager
import gorm.tools.support.MsgService
import gorm.tools.support.Results
import grails.rest.render.RenderContext

/**
 * Rederer for paged list data
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
class ResultsRenderer extends JsonGeneratorRenderer<Results>{

    @Autowired
    MsgService msgService

    ResultsRenderer() {
        super(Results)
    }

    @Override
    void render(Results result, RenderContext context) {
        setContentType(context)
        def builder = new StreamingJsonBuilder(context.writer, jsonGenerator)
        builder.call([
            ok: result.ok,
            code: result.getCode(),
            message: getMessage(result),
            success: collectMessages(result.success),
            failed: collectMessages(result.failed)
        ])
    }

    // swallow no such message exception and returns empty string
    String getMessage(Results result){
        try {
            msgService.getMessage(result)
        }catch(NoSuchMessageException e){
            //XXX is this what we want to do?
            return ""
        }
    }

    List collectMessages(List<Results> results){
        results.collect {
            [message: getMessage(it)]
        }.findAll{it.message}
    }
}
