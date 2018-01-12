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
import org.codehaus.groovy.runtime.InvokerHelper
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormStaticApi
import org.grails.datastore.mapping.model.PersistentEntity
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
@SuppressWarnings(['CatchException', 'VariableName'])
@CompileStatic
class EntityMapBinder extends GrailsWebDataBinder implements MapBinder {

    EntityMapBinder() {
        super(null)
    }

    EntityMapBinder(GrailsApplication grailsApplication) {
        super(grailsApplication)
    }

    /**
     * Binds data from a map on target object.
     *
     * @param obj The target object to bind
     * @param source The data binding source
     * @param bindAction
     */
    void bind(obj, DataBindingSource source, BindAction bindAction) {
        bind obj, source, null, getBindingIncludeList(obj), null, null, bindAction
    }

    /**
     * Binds data from a map on target object.
     *
     * @param obj The target object to bind
     * @param source The data binding source
     * @param listener DataBindingListener
     * @param bindAction BindAction
     */
    void bind(obj, DataBindingSource source, DataBindingListener listener, BindAction bindAction) {
        bind obj, source, null, getBindingIncludeList(obj), null, listener, bindAction
    }

    /**
     * Binds data from a map on target object.
     *
     * @param obj The target object to bind
     * @param source The data binding source
     * @param filter Only properties beginning with filter will be included in the data binding.
     * @param whiteList A list of property names to be included during this
     * data binding.  All other properties represented in the binding source
     * will be ignored
     * @param blackList A list of properties names to be excluded during
     * this data binding.
     * @param listener DataBindingListener
     * @param bindAction BindAction
     */
    void bind(object, DataBindingSource source, String filter, List whiteList, List blackList, DataBindingListener listener, BindAction bindAction) {
        Object bindingResult = new BeanPropertyBindingResult(object, object.getClass().name)
        doBind object, source, filter, whiteList, blackList, listener, bindingResult, bindAction
    }

    protected void doBind(object, DataBindingSource source, String filter, List whiteList, List blackList, DataBindingListener listener, errors, BindAction bindAction) {
        //TODO this is where we will store errors
        BeanPropertyBindingResult bindingResult = (BeanPropertyBindingResult)errors
        GrailsWebDataBindingListener errorHandlingListener = new GrailsWebDataBindingListener(messageSource)
        List<DataBindingListener> allListeners = []
        allListeners << errorHandlingListener

        DataBindingEventMulticastListener listenerWrapper = new DataBindingEventMulticastListener(allListeners)

        fastBind(object, source, whiteList, listenerWrapper, errors, bindAction)

        populateErrors(object, bindingResult)
    }

    /**
     * Binds data from a map on target object.
     *
     * @param args An optional map of options. supports two boolean options
     * <ul>
     *     <li><b>include</b> The list of properties to include in data binding</li>
     *     <li><b>errorHandling</b> If type conversion error should be handled and Added to Errors</li>
     * </ul>
     *
     * @param target The target object to bind
     * @param source The data binding source
     * @param bindAction BindAction
     */
    @Override
    void bind(Map args = [:], Object target, Map<String, Object> source) {
        BindAction bindAction = (BindAction) args["bindAction"]

        List includeList = (List) args["include"] ?: getBindingIncludeList(target)
        Boolean errorHandling = args["errorHandling"] == null ? true : args["errorHandling"]
        if(errorHandling) {
            bind target, new SimpleMapDataBindingSource(source), null, includeList, null, null, bindAction
        } else{
            fastBind target, new SimpleMapDataBindingSource(source), includeList, bindAction
        }
    }

    void fastBind(Object target, DataBindingSource source, List whiteList = null, DataBindingListener listener = null, errors = null, BindAction bindAction) {
        Objects.requireNonNull(target, "Target is null")
        if (!source) return
        //println whiteList
        GormStaticApi gormStaticApi = GormEnhancer.findStaticApi(target.getClass())
        PersistentEntity entity = gormStaticApi.gormPersistentEntity
        List<String> properties = whiteList ?: entity.persistentPropertyNames

        for (String prop : properties) {
            PersistentProperty perProp = entity.getPropertyByName(prop)
            try{
                setProp(target, source, perProp, listener, errors, bindAction)
            } catch (Exception e) {
                if(errors) {
                    addBindingError(target, perProp.name, source.getPropertyValue(perProp.name), e, listener, errors)
                } else {
                    throw e
                }
            }
        }

    }

    void setProp(Object target, DataBindingSource source, PersistentProperty prop, DataBindingListener listener = null, errors = null, BindAction bindAction){
        if (!source.containsProperty(prop.name)) return

        Object propValue = source.getPropertyValue(prop.name)
        Object valueToAssign = propValue

        if (propValue instanceof String) {
            String sval = propValue as String
            Class typeToConvertTo = prop.getType()

            if (sval == null || String.isAssignableFrom(typeToConvertTo)) {
                if (sval != null) {
                    sval = sval.trim()
                    sval = ("" == sval) ? null : sval
                }
                valueToAssign = sval
            } else if (Date.isAssignableFrom(typeToConvertTo)) {
                valueToAssign = IsoDateUtil.parse(sval)
            } else if (LocalDate.isAssignableFrom(typeToConvertTo)) {
                valueToAssign = IsoDateUtil.parseLocalDate(sval)
                //LocalDate.parse(val, DateTimeFormatter.ISO_DATE_TIME)
            } else if (LocalDateTime.isAssignableFrom(typeToConvertTo)) {
                valueToAssign = IsoDateUtil.parseLocalDateTime(sval)
            } else if (Number.isAssignableFrom(typeToConvertTo)) {
                valueToAssign = sval.asType(typeToConvertTo)
            } else if (conversionHelpers.containsKey(typeToConvertTo)) {
                List<ValueConverter> convertersList = conversionHelpers.get(typeToConvertTo)
                ValueConverter converter = convertersList?.find { ValueConverter c -> c.canConvert(propValue) }
                if (converter) {
                    valueToAssign = converter.convert(propValue)
                }
            } else if (conversionService?.canConvert(propValue.getClass(), typeToConvertTo)) {
                valueToAssign = conversionService.convert(propValue, typeToConvertTo)
            }

            target[prop.name] = valueToAssign

        } else if (prop instanceof Association) {
            bindAssociation(target, valueToAssign, (Association)prop, listener, errors, bindAction)
        } else {
            target[prop.name] = valueToAssign
        }

    }

    void bindAssociation(Object target, def value, Association association, DataBindingListener listener = null, errors = null, BindAction bindAction) {
        Object idValue =  isDomainClass(value.getClass()) ? value['id'] : getIdentifierValueFrom(value)
        if(bindAction == BindAction.Update) {

            if (association.getType().isAssignableFrom(value.getClass())) {
                target[association.name] = value
            }

            else if (idValue != 'null' && idValue != null && idValue != '') {
                Object instace = getPersistentInstance(getDomainClassType(target, association.name), idValue)
                target[association.name] = instace
                if(association.isOwningSide() && value instanceof Map && instace) {
                    //TODO pass listener and errors
                    fastBind(instace, new SimpleMapDataBindingSource(value), null, listener, errors, bindAction)
                }
            }
        }

        else if (bindAction == BindAction.Create) {
            Object instance
            if(association.isOwningSide() && value instanceof Map) {
                instance = association.type.newInstance()
            } else if (association.getType().isAssignableFrom(value.getClass())) {
                instance = value
            }
            else {
                instance = getPersistentInstance(getDomainClassType(target, association.name), idValue)
            }

            if(value instanceof Map && instance) fastBind(instance, new SimpleMapDataBindingSource((Map)value), bindAction)
            target[association.name] = instance
        }

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


    @Override
    protected getPersistentInstance(Class<?> type, id) {
        try {
            InvokerHelper.invokeStaticMethod type, 'load', id
        } catch (Exception exc) {}
    }

}
