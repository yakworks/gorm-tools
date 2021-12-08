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

@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class CSVPathKeyMapReader extends CSVReaderHeaderAware {

    String pathDelimiter

    /**
     * Constructor with supplied reader.
     *
     * @param reader The reader to an underlying CSV source
     */
    CSVPathKeyMapReader(Reader reader) {
        super(reader);
        this.pathDelimiter = "."
    }

    CSVPathKeyMapReader(Reader reader, String delim) {
        super(reader);
        this.pathDelimiter = delim
    }

    /**
     * CSVPathKeyMapReader.of(reader).pathDelimiter('_')
     */
    static CSVPathKeyMapReader of(Reader reader) {
        new CSVPathKeyMapReader(reader)
    }

    @Override
    Map<String, String> readMap() {
        Map<String, String> data = super.readMap()
        return (Map<String, String>) new PathKeyMap(data, pathDelimiter)
    }

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

    //just a helper which can be used by client code to iterate without needing CsvIterator
    boolean hasNext() {
        return hasNext
    }
}
