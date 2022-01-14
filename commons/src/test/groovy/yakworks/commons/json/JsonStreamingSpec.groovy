/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.json

import groovy.json.JsonOutput
import groovy.json.StreamingJsonBuilder
import org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport
import spock.lang.Specification
import yakworks.commons.lang.IsoDateUtil
import yakworks.commons.util.BuildSupport

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * sanity checks for streaming to a file
 */
class JsonStreamingSpec extends Specification implements JsonEngineTrait {

    static final String API_BUILD = 'build/api-docs'

    Path getJsonFile(String file = 'streamed.json'){
        return Paths.get(BuildSupport.gradleProjectDir, "build/$file")
    }

    Map generateData(Long id) {
        return [
            num: "$id",
            name: "Sink$id",
            name2: (id % 2) ? "SinkName2-$id" + id : null,
            inactive: (id % 2 == 0),
            amount: (id - 1) * 1.25,
            // actDate: LocalDateTime.now().plusDays(id).toDate(),
            localDate: IsoDateUtil.format(LocalDate.now().plusDays(id)),
            localDateTime: IsoDateUtil.format(LocalDateTime.now().plusDays(id)),
            ext:[ name: "SinkExt$id"],
            bazMap: [foo: 'bar']
            // thing: [id: id]
        ]
    }

    List<Map> generateDataList(int numRecords) {
        List<Map> list = []
        (1..numRecords).each { int index ->
            list << generateData(index)
        }
        return list
    }

    void flushAndClose(Writer writer){
        try {
            writer.flush();
        } catch (IOException e) {
            // try to continue even in case of error
        }
        DefaultGroovyMethodsSupport.closeWithWarning(writer);
    }

    void "sanity check stream file"() {
        when:
        def path = getJsonFile()
        Files.deleteIfExists(path)
        def fileWriter = path.newWriter()
        def sjb = new StreamingJsonBuilder(fileWriter, jsonGenerator)

        sjb.call {
            ok true
            status 'foo'
            code 'bar'
            title 'baz'
        }

        flushAndClose(fileWriter)

        then:
        Files.exists(path)
        path.getText().contains('"ok":true,"status":"foo"')

    }

    void "sanity check multiple calls"() {
        when:
        def path = getJsonFile("multi.json")
        Files.deleteIfExists(path)
        def writer = path.newWriter()
        def sjb = new StreamingJsonBuilder(writer, jsonGenerator)
        def dataList = generateDataList(100)

        // test writing out in chunks
        writer.write('[\n')
        dataList.each {

            sjb.call it
            writer.write(',\n')
        }
        writer.write(']')

        flushAndClose(writer)

        then:
        Files.exists(path)
        path.getText().startsWith('[\n{"num":"1","name":"Sink1"')
        path.getText().endsWith('},\n]')
    }

    void "test streamToFile"() {
        when:
        def path = getJsonFile("multi.json")
        Files.deleteIfExists(path)
        def dataList = generateDataList(100)
        JsonStreaming.streamToFile(dataList, path)

        then:
        Files.exists(path)
        path.getText().startsWith('[\n{"num":"1","name":"Sink1"')
        path.getText().endsWith('},\n]')
    }

    void "sanity check merging files"() {
        when:
        def path1 = getJsonFile("merge1.txt")
        def path2 = getJsonFile("merge2.txt")
        def path3 = getJsonFile("merge3.txt")
        path1.setText("foo")
        path2.setText("bar")
        path3.setText("baz")

        [path2, path3].each{
            it.withReader { reader ->
                path1.append(reader)
            }
        }

        then:
        Files.exists(path1)
        path1.getText() == 'foobarbaz'

    }

    void "append files"() {
        when:
        def path = getJsonFile()
        Files.deleteIfExists(path)
        def fileWriter = path.newWriter()
        def sjb = new StreamingJsonBuilder(fileWriter, jsonGenerator)

        sjb.call {
            ok true
            status 'foo'
            code 'bar'
            title 'baz'
        }

        flushAndClose(fileWriter)

        then:
        Files.exists(path)
        path.getText().contains('"ok":true,"status":"foo"')
    }
}
