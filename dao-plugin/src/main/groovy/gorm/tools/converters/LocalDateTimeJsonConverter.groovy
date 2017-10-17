package gorm.tools.converters

import grails.plugin.json.builder.JsonConverter
import groovy.transform.CompileStatic

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@CompileStatic
class LocalDateTimeJsonConverter implements JsonConverter {

	@Override
	Closure<? extends CharSequence> getConverter() {
		{ LocalDateTime date ->
			"\"${DateTimeFormatter.ISO_LOCAL_DATE_TIME.format((LocalDateTime) date)}\""
		}
	}

	@Override
	Class getType() {
		LocalDateTime
	}
}
