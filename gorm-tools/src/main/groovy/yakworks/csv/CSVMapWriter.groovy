/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.csv

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import com.opencsv.CSVWriter
import yakworks.commons.beans.PropertyTools
import yakworks.commons.map.MapFlattener
import yakworks.meta.MetaMapList

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

    void writeHeader(Set<String> headerKeys){
        writeNext(csvWriter, headerKeys)
    }

    /**
     * If dataList is a MetaMapList then it will use its metaEntity to get the headers.
     * otherwise it creates the header using the the data in the first item of collection.
     */
    CSVMapWriter createHeader(Collection<Map> dataList){
        if(dataList instanceof MetaMapList && dataList.metaEntity){
            headers = dataList.metaEntity.flattenProps()
            writeHeader(headers)
        } else {
            Map<String, Object> firstRow = dataList[0] as Map<String, Object>
            Map flatRow = MapFlattener.of(firstRow).convertObjectToString(false).flatten()
            headers = flatRow.keySet()
            writeHeader(headers)
        }
        return this
    }

    CSVMapWriter writeCsv(Collection<Map> dataList){
        dataList.eachWithIndex{ row, int i->
            List vals1 = collectVals(row as Map, headers)
            writeCollection(vals1)

            //flush every 1000
            if (i % 1000 == 0) {
                csvWriter.flush()
            }
        }
        return this
    }

    List collectVals(Map map, Set<String> headers) {
        List vals = headers.collect {
            //PropertyUtils.getProperty(map, it)
            PropertyTools.getProperty(map, it)
        }
        return vals
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
