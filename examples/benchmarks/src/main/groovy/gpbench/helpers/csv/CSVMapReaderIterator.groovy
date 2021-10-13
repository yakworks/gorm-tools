/*
 * Copyright 2010 Joshua Burnett
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * under the License.
 */
package gpbench.helpers.csv

/**
 * Class the read csv and return the rows as a map assuming the first row has the field/key names or you explicitly set fieldKeys property
 * Also removes blank lines as for whatever reason the CSVReader does not
 * Uses an underlying OpenCSV's CSVReader.
 *
 * @since 0.2
 * @author Joshua Burnett
 */

class CSVMapReaderIterator implements Iterator {
    private CSVMapReader mapReader
    def nextEl

    CSVMapReaderIterator(CSVMapReader reader) throws IOException {
        this.mapReader = reader;
        mapReader.initFieldKeys()
        nextEl = mapReader.batchSize? mapReader.readNextBatch() :mapReader.readNext()
    }

    boolean hasNext() {
        if(!nextEl) mapReader.close()
        return nextEl
    }

    def next() {
        def curEl = nextEl
        try {
            nextEl = mapReader.batchSize? mapReader.readNextBatch() :mapReader.readNext()
        } catch (IOException e) {
            mapReader.close()
            throw new RuntimeException(e);
        }
        return curEl;
    }

    void remove() {
        throw new UnsupportedOperationException("This is a read only iterator.");
    }

}
