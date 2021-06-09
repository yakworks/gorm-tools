package yakworks.rally.common

import spock.lang.Ignore
import spock.lang.Specification
import yakworks.rally.tag.model.Tag

class NameCodeDescriptionSpec extends Specification {

    @Ignore // how to do test  if nameCodeDescription is interface ? I put test under TagSpec for now
    void "test code regex"() {
        when:
        NameCodeDescription nameCodeDescription = NameCodeDescription.create('code': 'aA12-_', name: 'tag')

        then:
        nameCodeDescription.validate()

        when:
        nameCodeDescription.code = 'aA 12-_'

        then:
        !nameCodeDescription.validate()

        when:
        nameCodeDescription.code = 'aA12-_*()'

        then:
        !nameCodeDescription.validate()

        when:
        nameCodeDescription.code = '1234567890A'

        then:
        !nameCodeDescription.validate()
    }

}
