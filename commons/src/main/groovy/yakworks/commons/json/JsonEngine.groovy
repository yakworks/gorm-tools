/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.json

import groovy.json.JsonGenerator
import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import yakworks.commons.json.converters.CurrencyConverter
import yakworks.commons.json.converters.InstantJsonConverter
import yakworks.commons.json.converters.LocalDateJsonConverter
import yakworks.commons.json.converters.LocalDateTimeJsonConverter
import yakworks.commons.json.converters.LocalTimeJsonConverter
import yakworks.commons.json.converters.OffsetDateTimeJsonConverter
import yakworks.commons.json.converters.OffsetTimeJsonConverter
import yakworks.commons.json.converters.PeriodJsonConverter
import yakworks.commons.json.converters.ZonedDateTimeJsonConverter

/**
 * Json Parser
 */
@Builder(builderStrategy= SimpleStrategy, prefix="")
@CompileStatic
class JsonEngine {

    private static JsonEngine INSTANCE

    String dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"

    String timeZone = "GMT"

    String locale = "en/US"

    Boolean escapeUnicode = false

    JsonGenerator jsonGenerator

    // JsonEngine(){ }

    JsonEngine build() {

        JsonGenerator.Options options = new JsonGenerator.Options()

        if (!escapeUnicode) {
            options.disableUnicodeEscaping()
        }
        Locale loc
        String[] localeData = locale.split('/')
        if (localeData.length > 1) {
            loc = new Locale(localeData[0], localeData[1])
        } else {
            loc = new Locale(localeData[0])
        }
        options.dateFormat(dateFormat, loc)
        options.timezone(timeZone)
        options.excludeNulls()

        getConverters().each {
            options.addConverter(it)
        }

        jsonGenerator = options.build()
        return this
    }

    List<JsonGenerator.Converter> getConverters(){
        ServiceLoader<JsonGenerator.Converter> loader = ServiceLoader.load(JsonGenerator.Converter.class);
        List<JsonGenerator.Converter> converters = []
        for (JsonGenerator.Converter converter : loader) {
            converters.add(converter)
        }
        converters.add(new InstantJsonConverter())
        converters.add(new LocalDateJsonConverter())
        converters.add(new LocalDateTimeJsonConverter())
        converters.add(new LocalTimeJsonConverter())
        converters.add(new OffsetDateTimeJsonConverter())
        converters.add(new OffsetTimeJsonConverter())
        converters.add(new PeriodJsonConverter())
        converters.add(new ZonedDateTimeJsonConverter())
        converters.add(new CurrencyConverter())
        // OrderComparator.sort(converters)
        return converters
    }

    static String toJson(Object object){
        stringify(object)
    }

    static String stringify(Object object, Map arguments = [:]){
        getGenerator().toJson(object)
    }

    static JsonGenerator getGenerator(){
        if(!INSTANCE) INSTANCE = new JsonEngine().build()
        INSTANCE.jsonGenerator
    }

}
