package gorm.tools.beans

import grails.databinding.converters.ValueConverter
import groovy.transform.CompileStatic
import org.apache.commons.lang.time.DateUtils
import org.springframework.core.annotation.Order

/**
 * Used for binding date properties from string
 *
 * So if property is Date and the value that should be bind to it is string, then it will be converted by default before
 * binding
 *
 */
@Order(value = 1)
@CompileStatic
class MultiFormatDateConverter implements ValueConverter {
    /**
     * allowEmpty = true(default) empty string will get set to null,
     * if false then empty string throws IllegalArgumentException gets thrown in that case.
     */
    boolean allowEmpty = true

    //DateUtils.parseDate will do these in order
    String[] formats = ['MM/dd/yy', 'yyyy-MM-dd']

    @Override
    boolean canConvert(Object value) {
        return (value instanceof String)
    }

    /**
     * Converts input string to date format based on formats
     *
     * @param value date value
     * @return parsed date if value is string, value in other case
     */
    @Override
    Object convert(Object value) {
        Date dateValue = null
        if (value instanceof String) {
            String input = (String) value
            if (this.allowEmpty && !input) return dateValue
            if (DateUtil.GMT_MILLIS.matcher(input).matches() || DateUtil.GMT_SECONDS.matcher(input).matches() || DateUtil.TZ_LESS.matcher(input).matches()) {
                dateValue = DateUtil.parseJsonDate(input)
            } else {
                dateValue = DateUtils.parseDate(input, formats)
            }
        }

        return dateValue

    }

    /**
     * The type converter should be applied to
     *
     * @return Date
     */
    @Override
    Class<?> getTargetType() {
        return Date
    }
}