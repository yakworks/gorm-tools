package gpbench.helpers

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
abstract class RecordsLoader {
    Map<String, List> _cache = [:]

    List<Map> read(String file) {
        if(_cache.containsKey(file)) {
            log.trace("Records $file found in cache")
            return _cache.get(file)
        }
        else {
            log.trace("Records $file not found in cache")
            List<Map> records = load(file)
            records = Collections.unmodifiableList(records)
            _cache.put(file, records)
            return records
        }
    }

    abstract List<Map> load(String file)
}
