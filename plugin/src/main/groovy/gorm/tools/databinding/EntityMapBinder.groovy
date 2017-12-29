package gorm.tools.databinding

import gorm.tools.beans.IsoDateUtil
import grails.core.GrailsApplication
import grails.databinding.DataBindingSource
import grails.databinding.converters.ValueConverter
import grails.databinding.events.DataBindingListener
import grails.web.databinding.GrailsWebDataBinder
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormStaticApi
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.web.databinding.DataBindingEventMulticastListener
import org.grails.web.databinding.GrailsWebDataBindingListener
import org.springframework.validation.BeanPropertyBindingResult

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Faster data binder for PersistentEntity.persistentProperties. Uses the persistentProperties to assign values from the Map
 * Explicitly checks and converts most common property types eg (numbers and dates). Otherwise fallbacks to value converters.
 *
 */
@CompileStatic
class EntityMapBinder extends GrailsWebDataBinder implements MapBinder {
    private static final String ID_PROP = "id"

    EntityMapBinder() {
        super(null)
    }

    EntityMapBinder(GrailsApplication grailsApplication) {
        super(grailsApplication)
    }

    @Override
    protected void doBind(object, DataBindingSource source, String filter, List whiteList, List blackList, DataBindingListener listener, errors) {
        BeanPropertyBindingResult bindingResult = (BeanPropertyBindingResult)errors
        def errorHandlingListener = new GrailsWebDataBindingListener(messageSource)

        List<DataBindingListener> allListeners = []
        allListeners << errorHandlingListener
        if(listener != null && !(listener instanceof DataBindingEventMulticastListener)) {
            allListeners << listener
        }
        allListeners.addAll listeners.findAll { DataBindingListener l -> l.supports(object.getClass()) }

        def listenerWrapper = new DataBindingEventMulticastListener(allListeners)

        boolean bind = listenerWrapper.beforeBinding(object, bindingResult)

        if (bind) {
            //super.doBind object, source, filter, whiteList, blackList, listenerWrapper, bindingResult
        }

        listenerWrapper.afterBinding object, bindingResult

        populateErrors(object, bindingResult)
    }

    void bind(Object target, Map<String, Object> source, BindAction bindAction = null) {
        fastBind(target, source)
    }

    void fastBind(Object target, Map<String, Object> source) {
        Objects.requireNonNull(target, "Target is null")
        if (!source) return

        GormStaticApi gormStaticApi = GormEnhancer.findStaticApi(target.getClass())
        List<PersistentProperty> properties = gormStaticApi.gormPersistentEntity.persistentProperties

        for (PersistentProperty prop : properties) {
            if (!source.containsKey(prop.name)) continue
            Object value = source[prop.name]
            Object valueToAssign = value

            if (prop instanceof Association && value[ID_PROP]) {
                valueToAssign = GormEnhancer.findStaticApi(((Association) prop).associatedEntity.javaClass).load(value[ID_PROP] as Long)
            } else if (value instanceof String) {
                String val = value as String
                Class typeToConvertTo = prop.getType()
                if (String.isAssignableFrom(typeToConvertTo)) {
                    valueToAssign = val
                }
                else if (Number.isAssignableFrom(typeToConvertTo)) {
                    valueToAssign = val.asType(typeToConvertTo)
                }
                else if (Date.isAssignableFrom(typeToConvertTo)) {
                    valueToAssign = IsoDateUtil.parse(val)
                }
                else if (LocalDate.isAssignableFrom(typeToConvertTo)) {
                    valueToAssign = LocalDate.parse(val)
                }
                else if (LocalDateTime.isAssignableFrom(typeToConvertTo)) {
                    valueToAssign = LocalDateTime.parse(val, DateTimeFormatter.ISO_DATE_TIME)
                }
                else if (conversionHelpers.containsKey(typeToConvertTo)) {
                    List<ValueConverter> convertersList = conversionHelpers.get(typeToConvertTo)
                    ValueConverter converter = convertersList?.find { ValueConverter c -> c.canConvert(value) }
                    if (converter) {
                        valueToAssign = converter.convert(value)
                    }
                } else if (conversionService?.canConvert(value.getClass(), typeToConvertTo)) {
                    valueToAssign = conversionService.convert(value, typeToConvertTo)
                }
            }

            target[prop.name] = valueToAssign

        }

    }

}
