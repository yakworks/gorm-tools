package grails.plugin.dao.converters

import grails.plugin.json.builder.JsonConverter
import groovy.transform.CompileStatic

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@CompileStatic
class ZonedDateTimeJsonConverter implements JsonConverter {

	@Override
	Closure<? extends CharSequence> getConverter() {
		{ ZonedDateTime date ->
			"\"${DateTimeFormatter.ISO_ZONED_DATE_TIME.format((ZonedDateTime) date)}\""
		}
	}

	@Override
	Class getType() {
		ZonedDateTime
	}
}
