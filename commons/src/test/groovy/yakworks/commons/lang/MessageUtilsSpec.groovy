package yakworks.commons.lang

import spock.lang.Specification

class MessageUtilsSpec extends Specification {

    void "label keys from path"() {
        expect:
        results == MessageUtils.labelKeysFromPath('ar', path)

        where:
        results                                                                                         | path
        ['ar.name', 'name']                                                                             | 'name'
        ['ar.status.name', 'status.name', 'name']                                                       | 'status.name'
        ['ar.org.calc.totalDue', 'org.calc.totalDue', 'calc.totalDue', 'totalDue']                      | 'org.calc.totalDue'
        ['ar.org.member.branch.num', 'org.member.branch.num', 'member.branch.num', 'branch.num', 'num'] | 'org.member.branch.num'
    }

}
