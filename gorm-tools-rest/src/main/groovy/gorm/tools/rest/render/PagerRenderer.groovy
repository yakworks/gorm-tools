/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.render

import groovy.json.StreamingJsonBuilder
import groovy.transform.CompileStatic

import gorm.tools.beans.Pager
import grails.rest.render.RenderContext

/**
 * Rederer for paged list data
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
class PagerRenderer extends JsonGeneratorRenderer<Pager>{

    PagerRenderer() {
        super(Pager)
    }

    @Override
    void render(Pager pager, RenderContext context) {
        setContentType(context)

        def builder = new StreamingJsonBuilder(context.writer, jsonGenerator)
        builder.call([
            page: pager.page,
            total: pager.getPageCount(),
            records: pager.recordCount,
            data: pager.data
        ])
    }

}
