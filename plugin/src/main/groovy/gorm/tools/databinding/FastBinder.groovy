package gorm.tools.databinding

import gorm.tools.beans.DateUtil
import grails.databinding.converters.ValueConverter
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormStaticApi
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.springframework.beans.factory.annotation.Autowired

/**
 * Faster data binder. Copies properties from source to target object.
 * Explicitely checks and converts most common property types eg (numbers and dates). Otherwise fallbacks to value converters.
 *
 */
@CompileStatic
class FastBinder {
	private static final String ID_PROP = "id"

	protected Map<Class, List<ValueConverter>> conversionHelpers = [:].withDefault { c -> [] }

    def bind(String method, Object target, Map<String, Object> source, boolean ignoreAssociations = false) {
        //TODO will implement ediffernt logic based on if its a new isntance or an update. Method can be "Create" or
        "Update"
        bind(target, source, ignoreAssociations)
    }

	def bind(Object target, Map<String, Object> source, boolean ignoreAssociations = false) {
		Objects.requireNonNull(target, "Target is null")
		if(!source) return

		GormStaticApi gormStaticApi = GormEnhancer.findStaticApi(target.getClass())
		List<PersistentProperty> properties = gormStaticApi.gormPersistentEntity.persistentProperties

		for(PersistentProperty prop : properties) {
			if(!source.containsKey(prop.name)) continue
			def value = source[prop.name]
			def valueToAssign = value

			if(prop instanceof Association && !ignoreAssociations && value[ID_PROP]) {
				valueToAssign = GormEnhancer.findStaticApi(((Association)prop).associatedEntity.javaClass).load(value[ID_PROP] as Long)
			}

			else if (value instanceof String) {
				Class typeToConvertTo = prop.getType()
				if(Number.isAssignableFrom(typeToConvertTo)){
					valueToAssign = (value as String).asType(typeToConvertTo)
				}

				else if(Date.isAssignableFrom(typeToConvertTo)){
					valueToAssign = DateUtil.parseJsonDate(value as String)
				}
				else if(conversionHelpers.containsKey(typeToConvertTo)){
					List<ValueConverter> convertersList = conversionHelpers.get(typeToConvertTo)
					ValueConverter converter = convertersList?.find { ValueConverter c -> c.canConvert(value) }
					if (converter) {
						valueToAssign = converter.convert(value)
					}
				}
			}

			target[prop.name] = valueToAssign

		}

		return target

	}

	@Autowired(required=true)
	void setValueConverters(ValueConverter[] converters) {
		converters.each { ValueConverter converter ->
			registerConverter converter
		}
	}

	void registerConverter(ValueConverter converter) {
		conversionHelpers[converter.targetType] << converter
	}

}
