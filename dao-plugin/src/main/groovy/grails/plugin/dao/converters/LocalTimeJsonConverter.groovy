package grails.plugin.dao.converters

import grails.plugin.json.builder.JsonConverter
import groovy.transform.CompileStatic

import java.time.LocalTime
import java.time.format.DateTimeFormatter

@CompileStatic
class LocalTimeJsonConverter implements JsonConverter {

	@Override
	Closure<? extends CharSequence> getConverter() {
		{ LocalTime date ->
			"\"${DateTimeFormatter.ISO_LOCAL_TIME.format((LocalTime) date)}\""
		}
	}

	@Override
	Class getType() {
		LocalTime
	}
}
