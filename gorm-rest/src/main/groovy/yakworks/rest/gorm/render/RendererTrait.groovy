/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.render

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.GenericTypeResolver

import grails.rest.render.RenderContext
import grails.rest.render.Renderer
import yakworks.i18n.icu.ICUMessageSource

/**
 * Common base trait for Renderer with some helper methods and common autowired.
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.3.24
 */
@CompileStatic
trait RendererTrait<T> implements Renderer<T> {

    String encoding = 'UTF-8'

    @Autowired
    ICUMessageSource msgService

    Class<T> targetType

    @Override
    Class<T> getTargetType() {
        if (!targetType) this.targetType = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), RendererTrait)
        return targetType
    }

    /**
     * gets the params in the arguments of the RenderContext, when we create the ServletRenderContext it adds params to the arguments as
     * otherwise its a pain to get what the user passed as query params.
     */
    Map<String, Object> getParams(RenderContext context){
        context.getArguments()?.params as Map<String, Object>
    }

    abstract void render(T object, RenderContext context)
}
