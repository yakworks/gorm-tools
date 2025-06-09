/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.csv

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import com.opencsv.CSVReaderHeaderAware
import yakworks.commons.map.LazyPathKeyMap
import yakworks.commons.map.Maps

/**
 * Overrides the CSVReaderHeaderAware to read csv rows into the LazyPathKeyMap
 * which can then be used for the EntityMapBinder
 */
@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class CSVPathKeyMapReader extends CSVReaderHeaderAware {

    /**
     * the pathDelimiter is used when the headers are useing somthing like _ insted of dots
     * for the nested paths
     */
    String pathDelimiter = "."

    /**
     * true(default) to prune out any null or empty string fields from map,
     */
    boolean prune = true

    /**
     * Constructor with supplied reader.
     *
     * @param reader The reader to an underlying CSV source
     */
    CSVPathKeyMapReader(Reader reader) {
        super(reader)
    }

    /**
     * CSVPathKeyMapReader.of(reader).pathDelimiter('_')
     */
    static CSVPathKeyMapReader of(Reader reader) {
        new CSVPathKeyMapReader(reader)
    }

    static CSVPathKeyMapReader of(File file) {
        return of(new FileReader(file))
    }

    @Override
    Map<String, Object> readMap() {
        Map data = super.readMap()
        data = prune && data ? Maps.prune(data) : data
        data = convertNullStrings(data)
        // data = data as Map<String, String>
        return LazyPathKeyMap.of(data as Map<String, Object>, pathDelimiter)
    }

    /**
     * Read all the rows in CSV, we can't override the readAll in CVSReader as is return list of string array
     *
     * @return the list of maps for entire file
     */
    List<Map<String, Object>> readAllRows() {
        List result = [] as List<Map<String, Object>>
        while (hasNext) {
            Map r = readMap()
            if(r) result << r
        }
        return result
    }

    /**
     * Map row = pathKeyReader.readMap{ Map data ->
     *     data.lines = ...get lines from other file
     *}*/
    Map<String, Object> readMap(Closure closure) {
        Map data = readMap()
        closure(data)
        return data
    }

    /**
     * exposes the protected hasNext in CSVReader. helpful to be able to iterate without needing CsvIterator
     */
    boolean hasNext() {
        return hasNext
    }

    //removes the utf BOM/ZWNBSP character which are added by excel etc at the beginning of line
    String getNextLine() {
        String line = super.getNextLine()
        if(line) {
            line = line.replaceAll("\uFEFF", "").trim()
        }
        return line
    }

    /**
     * Converts, string with value "null" constant, to null
     */
    static <K, V> Map<K, V> convertNullStrings(Map<K, V> map) {
        if (!map) return map
        return map.collectEntries { k, v ->
            if (v instanceof String && v.trim() == "null") v = null
            else if (v instanceof Map) v = convertNullStrings(v)
            return [k, v]
        } as Map<K, V>
    }
}
