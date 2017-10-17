package gorm.tools.beans

import grails.databinding.converters.ValueConverter
import groovy.transform.CompileStatic
import org.apache.commons.lang.time.DateUtils
import org.springframework.core.annotation.Order

@Order(value = 1)
@CompileStatic
class MultiFormatDateConverter implements ValueConverter {
    /**
     * allowEmpty = true(default) empty string will get set to null,
     * if false then empty string throws IllegalArgumentException gets thrown in that case.
     */
    boolean allowEmpty = true

    //DateUtils.parseDate will do these in order
    String[] formats = ['MM/dd/yy', 'yyyy-MM-dd'] //, 'MM/dd/yy HH:mm', 'yyyy-MM-dd HH:mm' ]

    @Override
    boolean canConvert(Object value) {
        return (value instanceof String)
    }

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

    @Override
    Class<?> getTargetType() {
        return Date
    }
}