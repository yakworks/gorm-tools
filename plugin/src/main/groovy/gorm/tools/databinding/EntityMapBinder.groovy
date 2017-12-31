package gorm.tools.databinding

import gorm.tools.GormMetaUtils
import gorm.tools.beans.IsoDateUtil
import grails.core.GrailsApplication
import grails.databinding.DataBindingSource
import grails.databinding.SimpleMapDataBindingSource
import grails.databinding.converters.ValueConverter
import grails.databinding.events.DataBindingListener
import grails.gorm.validation.ConstrainedProperty
import grails.gorm.validation.DefaultConstrainedProperty
import grails.util.Environment
import grails.web.databinding.GrailsWebDataBinder
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormStaticApi
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association

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
    void bind(obj, DataBindingSource source) {
        bind obj, source, null, getBindingIncludeList(obj), null, null
    }

    @Override
    void bind(obj, DataBindingSource source, DataBindingListener listener) {
        bind obj, source, null, getBindingIncludeList(obj), null, listener
    }

    @Override
    protected void doBind(object, DataBindingSource source, String filter, List whiteList, List blackList, DataBindingListener listener, errors) {
        //TODO this is where we will store errors
        //BeanPropertyBindingResult bindingResult = (BeanPropertyBindingResult)errors
//        GrailsWebDataBindingListener errorHandlingListener = new GrailsWebDataBindingListener(messageSource)
//        List<DataBindingListener> allListeners = [errorHandlingListener]
//        DataBindingEventMulticastListener listenerWrapper = new DataBindingEventMulticastListener(allListeners)
//
        fastBind(object, source, whiteList)

        //populateErrors(object, bindingResult)
    }

    void bind(Map args = [:], Object target, Map<String, Object> source) {
        bind(target, new SimpleMapDataBindingSource(source))
    }

    void fastBind(Object target, DataBindingSource source, List whiteList = null) {
        Objects.requireNonNull(target, "Target is null")
        if (!source) return
        //println whiteList
        GormStaticApi gormStaticApi = GormEnhancer.findStaticApi(target.getClass())
        PersistentEntity entity = gormStaticApi.gormPersistentEntity
        List<String> properties = whiteList ?: entity.persistentPropertyNames

        for (String prop : properties) {
            setProp(target, source, entity.getPropertyByName(prop))
        }

    }

    void setProp(Object target, DataBindingSource source, PersistentProperty prop){
        if (!source.containsProperty(prop.name)) return

        Object value = source.getPropertyValue(prop.name)
        Object valueToAssign = value

        if (value instanceof String) {
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
        } else if (prop instanceof Association && value[ID_PROP]) {
            valueToAssign = GormEnhancer.findStaticApi(((Association) prop).associatedEntity.javaClass).load(value[ID_PROP] as Long)
        }

        target[prop.name] = valueToAssign
    }

    static List getBindingIncludeList(final Object object) {
        List<String> whiteList = []
        final Class<? extends Object> objectClass = object.getClass()
        if (CLASS_TO_BINDING_INCLUDE_LIST.containsKey(objectClass)) {
            whiteList = CLASS_TO_BINDING_INCLUDE_LIST.get objectClass
        } else {
            GormStaticApi gormStaticApi = GormEnhancer.findStaticApi(object.getClass())
            PersistentEntity entity = gormStaticApi.gormPersistentEntity
            List<PersistentProperty> properties = entity.persistentProperties
            Map<String, ConstrainedProperty> constraints = GormMetaUtils.findConstrainedProperties(entity)
            for (PersistentProperty prop : properties) {
                DefaultConstrainedProperty cp = (DefaultConstrainedProperty)constraints[prop.name]
                Boolean bindable = cp?.getMetaConstraintValue("bindable") as Boolean
                if(bindable == null || bindable == true) {
                    whiteList.add(prop.name)
                }
            }
            if (!Environment.getCurrent().isReloadEnabled()) {
                CLASS_TO_BINDING_INCLUDE_LIST.put objectClass, whiteList
            }
            //println whiteList
        }

        return whiteList
    }

}
