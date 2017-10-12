package gorm.tools.converters

import grails.plugin.json.builder.JsonConverter
import groovy.transform.CompileStatic

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@CompileStatic
class OffsetDateTimeJsonConverter implements JsonConverter {

	@Override
	Closure<? extends CharSequence> getConverter() {
		{ OffsetDateTime date ->
			"\"${DateTimeFormatter.ISO_OFFSET_DATE_TIME.format((OffsetDateTime) date)}\""
		}
	}

	@Override
	Class getType() {
		OffsetDateTime
	}
}
