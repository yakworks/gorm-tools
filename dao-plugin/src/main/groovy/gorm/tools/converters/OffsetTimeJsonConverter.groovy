package gorm.tools.converters

import grails.plugin.json.builder.JsonConverter
import groovy.transform.CompileStatic

import java.time.OffsetTime
import java.time.format.DateTimeFormatter

@CompileStatic
class OffsetTimeJsonConverter implements JsonConverter {

	@Override
	Closure<? extends CharSequence> getConverter() {
		{ OffsetTime date ->
			"\"${DateTimeFormatter.ISO_OFFSET_TIME.format((OffsetTime) date)}\""
		}
	}

	@Override
	Class getType() {
		OffsetTime
	}
}
