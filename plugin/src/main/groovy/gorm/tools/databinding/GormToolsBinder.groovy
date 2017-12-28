package gorm.tools.databinding

import gorm.tools.beans.IsoDateUtil
import grails.core.GrailsApplication
import grails.web.databinding.GrailsWebDataBinder

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//TODO rename later
class GormToolsBinder extends GrailsWebDataBinder {

    GormToolsBinder(GrailsApplication grailsApplication) {
        super(grailsApplication)
    }

    @Override
    protected Object convert(Class typeToConvertTo, Object value) {
        if (value != null && typeToConvertTo.isAssignableFrom(value)) return value
        else if (value instanceof String) {
            String val = value as String
            if (String.isAssignableFrom(typeToConvertTo)) {
                return value
            } else if (Number.isAssignableFrom(typeToConvertTo)) {
                return val.asType(typeToConvertTo)
            } else if (Date.isAssignableFrom(typeToConvertTo)) {
                return IsoDateUtil.parse(val)
            } else if (LocalDate.isAssignableFrom(typeToConvertTo)) {
                return LocalDate.parse(val)
            } else if (LocalDateTime.isAssignableFrom(typeToConvertTo)) {
                return LocalDateTime.parse(val, DateTimeFormatter.ISO_DATE_TIME)
            }
        } else {
            return super.convert(typeToConvertTo, value)
        }
    }
}
