package gorm.tools.csv

import com.opencsv.CSVReaderHeaderAware
import com.opencsv.exceptions.CsvValidationException
import gorm.tools.repository.model.DataOp
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy


@Builder(builderStrategy= SimpleStrategy, prefix="")
@CompileStatic
class CSVPathKeyMapReader extends CSVReaderHeaderAware {

    public String pathDelimiter

    /**
     * Constructor with supplied reader.
     *
     * @param reader The reader to an underlying CSV source
     */
     CSVPathKeyMapReader(Reader reader) {
        super(reader);
    }

    /**
     * CSVPathKeyMapReader.of(reader).pathDelimiter('_')
     */
    static CSVPathKeyMapReader of(Reader reader){
        new CSVPathKeyMapReader(reader)
    }

    @Override
    public Map<String, String> readMap() {
        Map data = super.readMap()
        //TODO constract PathKeyMap from data and pass in pathDelimiter('_')
        return resultMap;
    }

    /**
     * Map row = pathKeyReader.readMap{ Map data ->
     *     data.lines = ...get lines from other file
     * }
     */
    public Map<String, String> readMap(Closure closure) {
        Map data = readMap()
        closure(data);
        return data
    }
}
