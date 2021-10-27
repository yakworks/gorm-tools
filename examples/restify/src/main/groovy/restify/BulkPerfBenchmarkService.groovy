package restify

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import gorm.tools.repository.bulk.BulkableArgs
import yakworks.rally.orgs.repo.OrgRepo

@Slf4j
@Component
@CompileStatic
class BulkPerfBenchmarkService {

    @Autowired
    OrgRepo orgRepo

    void insert(int numRecords) {
        List data = generateOrgData(numRecords)
        Long id = orgRepo.bulk(data,  BulkableArgs.create(asyncEnabled: false))
    }

    List<Map> generateOrgData(int numRecords) {
        List list = []
        (1..numRecords).each { int index ->
            Map info = [phone: "p-$index"]
            Map contact = [firstName: "contact-$index", middleName:"contact-$index", lastName: "contact-$index"]
            list << [num:"org-$index", name: "org-$index", info: info, type:"Customer", contact:contact]
        }
        return list
    }
}
