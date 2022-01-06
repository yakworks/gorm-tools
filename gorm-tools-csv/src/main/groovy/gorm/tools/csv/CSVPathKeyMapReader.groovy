/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.csv

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import com.opencsv.CSVReaderHeaderAware
import gorm.tools.databinding.PathKeyMap

/**
 * Overrides the CSVReaderHeaderAware to read csv rows into the PathKeyMap
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
    Map<String, String> readMap() {
        Map<String, String> data = super.readMap()
        return PathKeyMap.of(data, pathDelimiter) as Map<String, String>
    }

    /**
     * Read all the rows in CSV, we can't override the readAll in CVSReader as is return list of string array
     *
     * @return the list of maps for entire file
     */
    List<Map<String, String>> readAllRows() {
        List result = []
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
    Map<String, String> readMap(Closure closure) {
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
}
