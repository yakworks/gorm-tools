package yakworks.rally.tag


import yakworks.gorm.testing.SecuritySpecHelper
import gorm.tools.testing.integration.DataIntegrationTest
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.tag.model.Tag

@Integration
@Rollback
class TagTest extends Specification implements DataIntegrationTest, SecuritySpecHelper {

    def "Tag create"(){
        when:
        Long id = Tag.create([name: 'Foo', code: 'Foo']).id
        flushAndClear()

        then:
        def t = Tag.get(id)
        t.name == 'Foo'
        //t.name2 == 'foo2'
        // c.createdDate
        // c.createdBy == 1
        // c.editedDate
        // c.editedBy == 1
    }

}
