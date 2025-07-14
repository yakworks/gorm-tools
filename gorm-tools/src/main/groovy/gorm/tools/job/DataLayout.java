package gorm.tools.job;

public enum DataLayout {
    /**
     * If dataLayout=Result then data is a list of Result/Problem objects.
     * Errors are mixed in and the syncJob.data is just a rendered list of the Result or Problem objects.
     * BulkImport uses Payload for example. This is considered deprecated.
     */
    Result,

    /**
    * If dataLayout=Payload then data is just a json list or map as the data it, and errors will be in the problems field.
     * BulkExport uses Payload for example.
     * dataLayout=Payload IS the recomended way.
     * When dataLayout=Payload then the rendering of the data is only list of whats in each results payload.
     * For example if processing export then this is the only one that really makes sense as it gives a list of what
     * the query ran, such as Invoices.
     * Would look as if the call was made to the rest endpoint for a list synchronously
     * Since data can only support a list of entities then any issues or errors get stored in the separate problems field,
     * syncjob.problems will be populated with error results
    */
    Payload

}
