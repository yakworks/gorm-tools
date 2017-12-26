package gorm.tools.databinding

import gorm.tools.beans.DateUtil
import grails.databinding.converters.ValueConverter
import grails.util.Environment
import groovy.transform.CompileStatic
import org.grails.databinding.converters.ConversionService
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormStaticApi
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.web.databinding.DefaultASTDatabindingHelper
import org.grails.web.databinding.SpringConversionServiceAdapter
import org.springframework.beans.factory.annotation.Autowired

import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap

/**
 * Faster data binder for PersistentEntity.persistentProperties. Uses the persistentProperties to assign values from the Map
 * Explicitly checks and converts most common property types eg (numbers and dates). Otherwise fallbacks to value converters.
 *
 */
@CompileStatic
class EntityMapBinder implements MapBinder {
    private static final Map<Class, List> CLASS_TO_BINDING_INCLUDE_LIST = new ConcurrentHashMap<Class, List>()
    private static final String ID_PROP = "id"

    boolean trimStrings = true
    boolean convertEmptyStringsToNull = true

    ConversionService conversionService = new SpringConversionServiceAdapter()

    protected Map<Class, List<ValueConverter>> conversionHelpers = [:].withDefault { c -> [] }

    @Override
    void bind(Object target, Map<String, Object> source, BindAction bindAction) {
        bind(target, source, null, getBindingIncludeList(target), null)
    }

    @Override
    void bind(Object target, Map<String, Object> source) {
        bind(target, source, null, getBindingIncludeList(target), null)
    }

    @Override
    void bind(Object target, Map<String, Object> source, List<String> whiteList, List<String> blackList) {
        bind(target, source, null, whiteList, blackList)
    }

    void bind(Object target, Map<String, Object> source, BindAction bindAction, List<String> whiteList, List<String> blackList) {
        Objects.requireNonNull(target, "Target is null")
        if (!source) return

        GormStaticApi gormStaticApi = GormEnhancer.findStaticApi(target.getClass())
        List<PersistentProperty> properties = gormStaticApi.gormPersistentEntity.persistentProperties

        for (PersistentProperty prop : properties) {
            if (!source.containsKey(prop.name) || !shouldBind(prop.name, whiteList, blackList)) continue
            Object value = preprocessValue(source[prop.name])
            Object valueToAssign = value

            if (prop instanceof Association && value[ID_PROP]) {
                valueToAssign = GormEnhancer.findStaticApi(((Association) prop).associatedEntity.javaClass).load(value[ID_PROP] as Long)
            } else if (value instanceof String) {
                Class typeToConvertTo = prop.getType()
                if (Number.isAssignableFrom(typeToConvertTo)) {
                    valueToAssign = (value as String).asType(typeToConvertTo)
                } else if (Date.isAssignableFrom(typeToConvertTo)) {
                    valueToAssign = DateUtil.parseJsonDate(value as String)
                } else if (conversionHelpers.containsKey(typeToConvertTo)) {
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

    protected boolean shouldBind(String propName, List whiteList, List blackList) {
        return !blackList?.contains(propName) && (!whiteList || whiteList.contains(propName))
    }

    //TODO
    void bindUpdate(Object target, Map<String, Object> source) {
        //for now just pass them on
        bind(target, source, BindAction.Update)
    }

    //TODO
    void bindCreate(Object target, Map<String, Object> source) {
        //for now just pass them on
        bind(target, source, BindAction.Create)
    }

    @Autowired(required = true)
    void setValueConverters(ValueConverter[] converters) {
        converters.each { ValueConverter converter ->
            registerConverter converter
        }
    }

    void registerConverter(ValueConverter converter) {
        conversionHelpers[converter.targetType] << converter
    }

    @SuppressWarnings(["EmptyCatchBlock", "BitwiseOperatorInConditional", "CatchException"])
    private List getBindingIncludeList(final Object object) {
        List includeList = Collections.emptyList()
        try {
            Class<? extends Object> objectClass = object.getClass()
            if (CLASS_TO_BINDING_INCLUDE_LIST.containsKey(objectClass)) {
                includeList = CLASS_TO_BINDING_INCLUDE_LIST.get(objectClass)
            } else {
                Field whiteListField = objectClass.getDeclaredField(DefaultASTDatabindingHelper.DEFAULT_DATABINDING_WHITELIST)
                if (whiteListField != null) {
                    if ((whiteListField.getModifiers() & Modifier.STATIC) != 0) {
                        Object whiteListValue = whiteListField.get(objectClass)
                        if (whiteListValue instanceof List) {
                            includeList = (List)whiteListValue
                        }
                    }
                }
                if (!Environment.getCurrent().isReloadEnabled()) {
                    CLASS_TO_BINDING_INCLUDE_LIST.put(objectClass, includeList)
                }
            }
        } catch (Exception e) {
        }
        return includeList
    }

    protected preprocessValue(propertyValue) {
        if(propertyValue instanceof CharSequence) {
            String stringValue = propertyValue.toString()
            if (trimStrings) {
                stringValue = stringValue.trim()
            }
            if (convertEmptyStringsToNull && "".equals(stringValue)) {
                stringValue = null
            }
            return stringValue
        }
        return propertyValue
    }

}

