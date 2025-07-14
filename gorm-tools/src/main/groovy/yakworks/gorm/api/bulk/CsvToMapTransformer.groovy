/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api.bulk

interface CsvToMapTransformer {


    /**
     * Reads CSV rows into maps
     *
     * @param bulkImportJobArgs the args with the specs for the CSV file
     *
     * @return List<Map>
     */
    List<Map> process(BulkImportJobArgs bulkImportJobArgs)
}
