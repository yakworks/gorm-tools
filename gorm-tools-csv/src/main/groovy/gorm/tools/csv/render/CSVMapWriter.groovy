/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.csv.render

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import com.opencsv.CSVWriter
import yakworks.commons.map.MapFlattener

@CompileStatic
class CSVMapWriter {
    CSVWriter csvWriter
    Set<String> headers

    CSVMapWriter(Writer outWriter){
        this.csvWriter = new CSVWriter(outWriter)
    }

    static CSVMapWriter of(Writer outWriter){
        return new CSVMapWriter(outWriter)
    }

    void flush(){
        csvWriter.flush()
    }

    void writeHeaders(Map<String, Object> firstItem){
        //filter out the key that has the detail
        headers = firstItem.keySet()
        // println "headers: $headers"
        writeHeaders(headers)
    }

    void writeHeaders(Set<String> headerKeys){
        writeNext(csvWriter, headerKeys)
    }

    void writeCsv(List<Map> dataList){

        //flatten
        Map<String, Object> firstRow = dataList[0] as Map<String, Object>
        Map flatRow = flattenMap(firstRow)
        writeHeaders(flatRow)

        dataList.eachWithIndex{ row, int i->
            //get all the values for the masterHeaders keys
            writeLine(flattenMap(row as Map))

            //flush every 1000
            if (i % 1000 == 0) {
                csvWriter.flush()
            }
        }
    }

    static Map flattenMap(Map map){
        MapFlattener.of(map).convertObjectToString(true).flatten()
    }

    void writeLine(Map data){
        def vals = headers.collect{ data[it] as String }
        writeCollection(vals)
    }

    void writeCollection(Collection vals){
        writeNext(csvWriter, vals)
    }

    // for some reason compileStatic fails trying to call this method
    @CompileDynamic
    static void writeAll(CSVWriter writer, Object lines){
        writer.writeAll(lines)
    }

    @CompileDynamic
    static void writeNext(CSVWriter writer, Collection vals){
        writer.writeNext(vals as String[])
    }

}
