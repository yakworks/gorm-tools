/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.common

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.context.annotation.Lazy
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

import yakworks.commons.io.ZipUtils
import yakworks.commons.lang.Validate
import yakworks.gorm.etl.csv.CSVPathKeyMapReader
import yakworks.gorm.etl.csv.CsvToMapTransformer
import yakworks.rally.attachment.model.Attachment

/**
 * Read CSV from attachment with CSVPathKeyMapReader and transforms to List of maps
 */
@Service @Lazy
@Slf4j
@CompileStatic
class DefaultCsvToMapTransformer implements CsvToMapTransformer {

    /**
     * Reads CSV rows into maps
     *
     * @param params map with
     *  - attachmentId
     *  - dataFilename : name of csv file inside zip
     *  - headerPathDelimiter : Header delimeter
     *
     * @return List<Map>
     */
    List<Map> process(Map params) {
        Long attachmentId = params.attachmentId as Long
        String dataFilename = params.dataFilename ?: "data.csv"
        String headerPathDelimiter = params.headerPathDelimiter ?: "."
        return processRows(attachmentId, dataFilename, headerPathDelimiter) as List<Map>
    }

    List<Map<String, Object>> processRows(Long attachmentId, String dataFileName, String delim) {
        Attachment attachment = Attachment.get(attachmentId)
        Validate.notNull(attachment, "Attachment not found : ${attachmentId}")

        Resource zipR = attachment.resource
        Validate.notNull(zipR)
        File zip = zipR.file
        Validate.notNull(zip)

        InputStream dataIn = ZipUtils.getZipEntryInputStream(zip, dataFileName)
        Validate.notNull(dataIn, "$dataFileName not found in zip")
        return processRows(dataIn, delim)
    }

    List<Map<String, Object>> processRows(InputStream dataIn, String delim) {
        CSVPathKeyMapReader dataRowsReader = CSVPathKeyMapReader.of(new InputStreamReader(dataIn)).pathDelimiter(delim)
        return dataRowsReader.readAllRows()
    }

}
