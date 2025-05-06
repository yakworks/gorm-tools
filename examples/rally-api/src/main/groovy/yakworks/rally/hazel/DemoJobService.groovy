package yakworks.rally.hazel

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.stereotype.Component

@Component
@Slf4j
@CompileStatic
class DemoJobService {

    void runJob(Long id){
        log.info("runJob called: $id")
        sleep(2000)
    }
}
