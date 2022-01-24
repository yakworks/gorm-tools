/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.json

import java.nio.file.Path

import groovy.json.StreamingJsonBuilder
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import yakworks.commons.io.IOUtils

/**
 * Json Parser
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@SuppressWarnings('FieldName')
@Builder(builderStrategy= SimpleStrategy, prefix="")
@CompileStatic
class JsonStreaming {

    /**
     * Streams a collection of maps to file, flushes and closes writer when finished.
     * Its assumed the path passed has directories already, will throw error if not
     *
     * @param payload the object to write to file
     * @param filePath the file as a Path object
     */
    static void streamToFile(Collection<Map> payload, Path path){
        def writer = path.newWriter()
        def sjb = new StreamingJsonBuilder(writer, JsonEngine.generator)
        writer.write('[\n')
        int i = 0
        for (Map entry : payload) {
            sjb.call(entry)
            writer.write(',\n')
            //to avoid OOM error flush every 1000 just in case
            if (i % 1000 == 0) {
                writer.flush()
            }
            i++
        }
        writer.write(']')
        IOUtils.flushAndClose(writer)
    }

}
