package grails.plugin.dao.converters

import grails.plugin.json.builder.JsonConverter
import groovy.transform.CompileStatic

import java.time.LocalDate
import java.time.format.DateTimeFormatter

@CompileStatic
class LocalDateJsonConverter implements JsonConverter {

    @Override
    Closure<? extends CharSequence> getConverter() {
        { LocalDate date ->
            "\"${DateTimeFormatter.ISO_LOCAL_DATE.format((LocalDate)date)}\""
        }
    }

    @Override
    Class getType() {
        LocalDate
    }
}
