package gorm.tools.databinding

import groovy.transform.CompileStatic

import org.springframework.beans.MutablePropertyValues
import org.springframework.beans.PropertyValues
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

    public <T> T bind(T obj, Map data){
        return bind(obj, new MutablePropertyValues(data))
    }

    public <T> T bind(T obj, PropertyValues propVals){
        DataBinder binder = new DataBinder(obj)
        binder.conversionService = conversionService
        binder.bind(propVals)
        return obj
    }
}
