/*
* Copyright 2010 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.csv

import com.opencsv.CSVParser
import com.opencsv.CSVReader

/**
 * Utility class for adding CSV parsing capability to core Java/Groovy classes (String, File, InputStream, Reader).
 *
 * @since 0.1* @author Les Hazlewood
 */
class CSVReaderUtils {


    static CSVReader toCsvReader(File file, Map settingsMap) {
        return toCsvReader(new FileInputStream(file), settingsMap)
    }

    static CSVReader toCsvReader(InputStream is, Map settingsMap) {
        def charset = settingsMap?.get('charset')
        InputStreamReader reader
        if (charset) {
            reader = new InputStreamReader(is, charset)
        } else {
            reader = new InputStreamReader(is)
        }
        return toCsvReader(reader, settingsMap)
    }

    static CSVReader toCsvReader(Reader r, Map settingsMap) {
        char separatorChar = (settingsMap?.get('separatorChar') ?: CSVParser.DEFAULT_SEPARATOR) as char
        char quoteChar = (settingsMap?.get('quoteChar') ?: CSVParser.DEFAULT_QUOTE_CHARACTER) as char
        char escapeChar = (settingsMap?.get('escapeChar') ?: CSVParser.DEFAULT_ESCAPE_CHARACTER) as char
        int skipLines = Math.max(0, (settingsMap?.get('skipLines') ?: 0) as int)

        boolean strictQuotes = CSVParser.DEFAULT_STRICT_QUOTES
        def mapValue = settingsMap?.get('strictQuotes')
        if (mapValue != null) {
            strictQuotes = mapValue as boolean
        }

        boolean ignoreLeadingWhiteSpace = CSVParser.DEFAULT_IGNORE_LEADING_WHITESPACE
        mapValue = settingsMap?.get('ignoreLeadingWhiteSpace')
        if (mapValue != null) {
            ignoreLeadingWhiteSpace = mapValue as boolean
        }

        return new CSVReader(r, separatorChar, quoteChar, escapeChar, skipLines, strictQuotes, ignoreLeadingWhiteSpace)
    }

    static CSVReader toCsvReader(String s, Map settingsMap) {
        return toCsvReader(new StringReader(s), settingsMap)
    }
}
