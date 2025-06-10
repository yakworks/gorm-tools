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
import yakworks.etl.CSVPathKeyMapReader
import yakworks.gorm.api.bulk.CsvToMapTransformer
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

        List<Map> rows
        //try-with-resources so it automatically closes and cleans up after itself
        try (InputStream ins = getInputStream(attachmentId, dataFilename)) {
            rows = processRows(ins, headerPathDelimiter) as List<Map>
        }
        return rows
    }

    /**
     * returns the InputStream for the attachment depending on whether its a zip or not
     */
    InputStream getInputStream(Long attachmentId, String dataFileName){
        Attachment attachment = Attachment.get(attachmentId)
        Validate.notNull(attachment, "Attachment not found : ${attachmentId}")
        Resource res = attachment.resource
        Validate.notNull(res)
        File file = res.file
        InputStream ins
        //if its a zip then get the file specified in dataFilename
        if(attachment.extension == 'zip') {
            ins = ZipUtils.getZipEntryInputStream(file, dataFileName)
            Validate.notNull(ins, "$dataFileName not found in zip")
        } else {
            ins = file.newInputStream()
        }
        return ins
    }

    /** uses the CSVPathKeyMapReader to actual read and process the csv */
    List<Map<String, Object>> processRows(InputStream dataIn, String delim) {
        CSVPathKeyMapReader dataRowsReader = CSVPathKeyMapReader.of(new InputStreamReader(dataIn)).pathDelimiter(delim)
        return dataRowsReader.readAllRows()
    }

}
