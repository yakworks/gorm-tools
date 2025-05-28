/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.databinding

import groovy.transform.CompileStatic

import org.springframework.beans.MutablePropertyValues
import org.springframework.beans.PropertyValues
import org.springframework.boot.convert.ApplicationConversionService
import org.springframework.core.convert.ConversionService
import org.springframework.core.convert.support.DefaultConversionService
import org.springframework.validation.DataBinder

/**
 * Use Springs DataBinder and the RelaxedConversionService to bind maps and PropertyValues
 * to an object
 */
@CompileStatic
class BasicDataBinder {

    ConversionService conversionService

    BasicDataBinder(){
        conversionService = new RelaxedConversionService(new DefaultConversionService())
    }

    public static <T> T bind(T obj, Map data){
        return bind(obj, new MutablePropertyValues(data))
    }

    public static <T> T bind(T obj, PropertyValues propVals){
        DataBinder binder = new DataBinder(obj)
        //binder.conversionService = conversionService
        binder.conversionService = ApplicationConversionService.getSharedInstance()
        binder.bind(propVals)
        return obj
    }
}
