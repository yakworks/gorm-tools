/*
* Copyright 2010 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.csv

import au.com.bytecode.opencsv.CSVReader

/**
 * Class the read csv and return the rows as a map assuming the first row has the field/key names or you explicitly set fieldKeys property
 * Also removes blank lines as for whatever reason the CSVReader does not
 * Uses an underlying OpenCSV's CSVReader.
 *
 * @since 0.2* @author Joshua Burnett
 */
class CSVMapReader implements Closeable, Iterable {

    CSVReader csvReader

    //the column names from the first header line in the file
    def fieldKeys = []

    /**
     * If this is >0 then on each iteration, instead of a map, will return a list of maps of this batchSize
     * and returnAll or toList will return a list of lists(batches) of maps based on the batchSize
     * For example: this is usefull if you have a million rows in the CSV and want each iteration to return a list(batch) of 50 rows(maps) at a time
     * so you can process inserts into the db in batches of 50.
     */
    Integer batchSize = 0

    CSVMapReader() {
    }

    /**
     * Constructs CSVMapReader from the reader
     *
     * @param reader the reader to an underlying CSV source.
     */
    CSVMapReader(Reader reader) {
        this(reader, null)
    }

    /**
     * Constructs CSVMapReader from the reader
     *
     * @param reader the reader to an underlying CSV source.
     * @param settingsMap map of settings for the underlying CSVReader.
     */
    CSVMapReader(Reader reader, Map settingsMap) {
        csvReader = CSVReaderUtils.toCsvReader(reader, settingsMap)
        if (settingsMap?.batchSize) batchSize = settingsMap?.batchSize?.toInteger()
    }

    /**
     * Constructs CSVMapReader using a CSVReader
     *
     * @param csvReader the constructed CSVReader
     */
    CSVMapReader(CSVReader csvr) {
        csvReader = csvr
    }

    //takes the first header line (assumed from firstreadline) from the file and sets the keys
    def initFieldKeys() {
        fieldKeys = fieldKeys ?: csvReader.readNext()
    }

    /**
     * Reads the next line from the buffer in CSVReader and converts to map.
     */
    Map readNext() throws IOException {

        Map result
        String[] nextLine = csvReader.readNext()
        //skip blank lines if there is only 1 token and its blank
        while (nextLine && isBlankLine(nextLine)) {
            nextLine = csvReader.readNext()
        }
        return convertArrayToMap(fieldKeys, nextLine)
    }

    /**
     * Reads the next batched chunk of lines from the buffer in CSVReader and return a list of maps.
     */
    List<Map> readNextBatch() throws IOException {
        def reslist = []
        Map map = readNext()
        while (map) {
            reslist.add map
            if (reslist.size() == batchSize) break
            map = readNext()
        }
        return reslist
    }

    //This is here to remain consitent with file's eachLine in case someone uses it. Just calls the each
    void eachLine(Closure closure) {
        if (batchSize > 0) throw new UnsupportedOperationException("batchSize is >0 so you are not getting lines but a list of lines(rows/maps). Use each instead")
        each(closure)
    }

    //true it only 1 element in array and its blank
    boolean isBlankLine(String[] tokenArray) {
        return (!tokenArray[0] && tokenArray.size() <= 1)
    }

    /**
     * Reads the entire file into a List with each element being a map where keys are the from the 1st header line in file
     *
     * @return a List of Maps (or list of lists of maps if batchSize>0) with each map representing a line of the
     */
    List readAll() {
        this.collect { it }
    }

    /**
     * a more groovy alternate to readAll (opencsv syntax) that returns this as a list of maps
     */
    List toList() {
        return readAll()
    }

    //groovy way to cast, can do something like " def list = new CSVMapReader(file) as List
    Object asType(Class type) {
        if (type == List) return readAll()
    }


    static Map convertArrayToMap(keys, tokens) {
        if (!tokens) return null
        def map = [:]
        for (i in 0..tokens.size() - 1) {
            map[keys[i]] = tokens[i]
        }
        return map
    }

    /**
     * Calls close on CSVReader and closes the underlying reader. You should do this if you are working with Stream or Files!
     */
    void close() throws IOException {
        csvReader.close()
    }

    Iterator iterator() {
        try {
            return new CSVMapReaderIterator(this)
        } catch (IOException e) {
            throw new RuntimeException(e)
        }
    }

}

class CSVMapReaderIterator implements Iterator {
    private CSVMapReader mapReader
    def nextEl

    CSVMapReaderIterator(CSVMapReader reader) throws IOException {
        this.mapReader = reader
        mapReader.initFieldKeys()
        nextEl = mapReader.batchSize ? mapReader.readNextBatch() : mapReader.readNext()
    }

    boolean hasNext() {
        if (!nextEl) mapReader.close()
        return nextEl
    }

    def next() {
        def curEl = nextEl
        try {
            nextEl = mapReader.batchSize ? mapReader.readNextBatch() : mapReader.readNext()
        } catch (IOException e) {
            mapReader.close()
            throw new RuntimeException(e)
        }
        return curEl
    }

    void remove() {
        throw new UnsupportedOperationException("This is a read only iterator.")
    }

}
