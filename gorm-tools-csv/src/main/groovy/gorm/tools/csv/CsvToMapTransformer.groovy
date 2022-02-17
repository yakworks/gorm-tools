/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.csv

import groovy.transform.CompileStatic

interface CsvToMapTransformer {


    /**
     * Reads CSV rows into maps
     *
     * @param params map with
     *  - attachmentId
     *  - dataFilename : name of csv file inside zip
     *  - headerPathDelimiter : Header delimeter
     *  - any other parameter required by the implementation
     *
     * @return List<Map>
     */
    List<Map> process(Map params)
}