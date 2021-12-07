/*
 * Copyright 2010 Les Hazlewood
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
package gorm.tools.csv

import com.opencsv.CSVReader

/**
 * Unit tests for the  {@link CSVReaderUtils}  class.
 *
 * @since 0.1
 * @author Les Hazlewood
 */

class CSVReaderUtilsTest extends GroovyTestCase{

    StringReader r

    public void setUp() {
        r = new StringReader("hello, world")
    }

    void testNewCsvReaderSeparatorChar() {
        CSVReader reader = CSVReaderUtils.toCsvReader(r, ['separatorChar': 'x'])
        assertEquals('x', reader.parser.separator)
    }

    void testNewCsvReaderQuoteChar() {
        CSVReader reader = CSVReaderUtils.toCsvReader(r, ['quoteChar': 'x'])
        assertEquals('x', reader.parser.quotechar)
    }

    void testNewCsvReaderEscapeChar() {
        CSVReader reader = CSVReaderUtils.toCsvReader(r, ['escapeChar': 'x'])
        assertEquals('x', reader.parser.escape)
    }

    void testNewCsvReaderStrictQuotes() {
        CSVReader reader = CSVReaderUtils.toCsvReader(r, ['strictQuotes': true])
        assertTrue(reader.parser.strictQuotes)

        reader = CSVReaderUtils.toCsvReader(r, ['strictQuotes': false])
        assertFalse(reader.parser.strictQuotes)
    }

    void testNewCsvReaderIgnoreLeadingWhiteSpace() {
        CSVReader reader = CSVReaderUtils.toCsvReader(r, ['ignoreLeadingWhiteSpace': true])
        assertTrue(reader.parser.ignoreLeadingWhiteSpace)

        CSVReader reader2 = CSVReaderUtils.toCsvReader(r, ['ignoreLeadingWhiteSpace': false])
        assertFalse(reader2.parser.ignoreLeadingWhiteSpace)
    }

    void testNewCsvReaderSkipLines() {
        CSVReader reader = CSVReaderUtils.toCsvReader(r, ['skipLines': 1])
        assertEquals(1, reader.skipLines)

        reader = CSVReaderUtils.toCsvReader(r, ['skipLines': 2])
        assertEquals(2, reader.skipLines)
    }
}
