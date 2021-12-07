/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.csv

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import com.opencsv.CSVReaderHeaderAware
import com.opencsv.exceptions.CsvValidationException
import gorm.tools.databinding.PathKeyMap
import gorm.tools.repository.model.DataOp

@Builder(builderStrategy= SimpleStrategy, prefix="")
@CompileStatic
class CSVPathKeyMapReader extends CSVReaderHeaderAware {

    public String pathDelimiter

    /**
     * Constructor with supplied reader.
     *
     * @param reader The reader to an underlying CSV source
     */
     CSVPathKeyMapReader(Reader reader) {
        super(reader);
    }

    /**
     * CSVPathKeyMapReader.of(reader).pathDelimiter('_')
     */
    static CSVPathKeyMapReader of(Reader reader){
        new CSVPathKeyMapReader(reader)
    }

    @Override
     Map<String, String> readMap(String delim = ".") {
        Map<String, String> data = super.readMap()
        return (Map<String, String>)new PathKeyMap(data, delim)
    }

    List<Map<String, String>> readAllRows() {
        List result = []
        while(hasNext) {
            result << readMap()
        }
        return result
    }

    /**
     * Map row = pathKeyReader.readMap{ Map data ->
     *     data.lines = ...get lines from other file
     * }
     */
    Map<String, String> readMap(Closure closure) {
        Map data = readMap()
        closure(data);
        return data
    }

    //just a helper which can be used by client code to iterate without needing CsvIterator
    boolean hasNext() {
        return hasNext
    }
}
