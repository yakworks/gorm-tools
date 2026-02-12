package gorm.tools.audit

import grails.core.GrailsApplication
import grails.gorm.transactions.Rollback
import spock.lang.Ignore
import spock.lang.IgnoreRest
import yakworks.security.auditable.AuditEventType
import yakworks.security.auditable.AuditLogContext
import yakworks.security.auditable.resolvers.AuditRequestResolver
import grails.spring.BeanBuilder
import grails.testing.mixin.integration.Integration
import org.springframework.test.annotation.DirtiesContext
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import yakworks.testing.gorm.model.KitchenSink

@Integration
@Rollback
class AuditableSpec extends Specification {
    GrailsApplication grailsApplication
    @Shared KitchenSink entity

    void setup() {
        entity = new KitchenSink(name: 'foo')
    }

    void "excluded properties are respected"() {
        expect:
        AuditLogContext.withConfig(excluded: ['p1', 'p2']) { entity.getLogExcluded() } == ['p1', 'p2'] as Set<String>
    }

    void "included properties are respected"() {
        expect:
        AuditLogContext.withConfig(included: ['p1', 'p2']) { entity.getLogIncluded() } == ['p1', 'p2'] as Set<String>
    }

    void "mask properties are respected"() {
        expect:
        AuditLogContext.withConfig(mask: ['p1', 'p2']) { entity.getLogMaskProperties() } == ['p1', 'p2'] as Set<String>
    }

    void "ignore events are respected"() {
        expect:
        AuditLogContext.withConfig(ignoreEvents: [AuditEventType.DELETE]) {
            entity.getLogIgnoreEvents()
        } == [AuditEventType.DELETE] as Set<AuditEventType>
    }

    void "entity id uses ident by default"() {
        expect:
        entity.getLogEntityId() == entity.ident()
    }

    @IgnoreRest
    @Unroll
    void "convert logged property to string with logIds enabled for #value"(Object value, String expected) {
        when:
        String result = entity.convertLoggedPropertyToString('name', value)

        then:
        result == expected

        where:
        value                 | expected
        AuditEventType.DELETE                                              | "DELETE"
        10                    | "10"
        1.29                  | "1.29"
        null                  | null
        "Foo"                 | "Foo"
        [1, 2, 3]             | "1, 2, 3"

        // Associated auditable
        new KitchenSink(name: 'bar', id:10)                                    | "[id:id]bar"
        [new KitchenSink(name: 'bar'), new KitchenSink(name: 'baz')]    | "[id:id]bar, [id:id]baz"
    }

    @Unroll
    void "convert logged property to string with logIds disabled for #value"(Object value, String expected) {
        when:
        String result = AuditLogContext.withConfig(logIds: false) {
            entity.convertLoggedPropertyToString('ignored', value)
        }

        then:
        result == expected

        where:
        value                 | expected
        AuditEventType.DELETE                                              | "DELETE"
        10                    | "10"
        1.29                  | "1.29"
        null                  | null
        "Foo"                 | "Foo"

        // Non-collection entity is still logged
        new KitchenSink(property: 'bar')                                    | "[id:id]bar"

        // Collection values are not logged
        [1, 2, 3] | "N/A"
        [new KitchenSink(property: 'bar'), new KitchenSink(property: 'baz')] | "N/A"
    }

    void "full class name logging is enabled when configured"(Boolean flag, result) {
        expect:
        AuditLogContext.withConfig(logFullClassName: flag) { entity.getLogClassName() } == result

        where:
        flag  | result
        true  | "test.TestEntity"
        false | "TestEntity"
    }

    void "logIds flag is respected by configuration"(Boolean flag, result) {
        expect:
        AuditLogContext.withConfig(logIds: flag) { entity.isLogAssociatedIds() } == result

        where:
        flag  | result
        true  | true
        false | false
    }

    void "verbose log is enabled for all events by flag"(Boolean flag, result) {
        expect:
        AuditLogContext.withConfig(verbose: flag) { entity.getLogVerboseEvents() } == result

        where:
        flag  | result
        true  | AuditEventType.values() as Set<AuditEventType>
        false | Collections.EMPTY_SET
    }

    void "verbose log flag is ignored if specific verboseEvents are given"(Boolean flag, result) {
        expect:
        AuditLogContext.withConfig(verbose: flag, verboseEvents: [AuditEventType.UPDATE]) {
            entity.getLogVerboseEvents()
        } == result

        where:
        flag  | result
        true  | [AuditEventType.UPDATE] as Set<AuditEventType>
        false | [AuditEventType.UPDATE] as Set<AuditEventType>
    }

    void "auditable property names omit excluded properties"() {
        given:
        KitchenSink sink = new KitchenSink(name: 'Aaron', name2: "Sudhir")
        def allProps = KitchenSink.gormPersistentEntity.persistentProperties*.name as Set<String>

        when:
        def props = AuditLogContext.withConfig(excluded: ['name']) { sink.getAuditablePropertyNames() } as Set<String>

        then:
        props == (allProps - "name")
    }

    void "auditable property names include only whitelist properties"() {
        given:
        KitchenSink sink = new KitchenSink(name: 'Aaron', name2: "Sudhir")

        when:
        def props = AuditLogContext.withConfig(included: ['name']) { sink.getAuditablePropertyNames() } as Set<String>

        then:
        props == ["name"] as Set<String>
    }

    void "auditable property names include whitelist even if excluded"() {
        given:
        KitchenSink sink = new KitchenSink(name: 'Aaron', name1: "Sudhir")

        when:
        def props = AuditLogContext.withConfig(included: ['name'], excluded: ['name']) { sink.getAuditablePropertyNames() } as Set<String>

        then:
        props == ["name"] as Set<String>
    }

    @DirtiesContext
    @Ignore
    void "uses the registered audit resolver bean"() {
        given:
        BeanBuilder bb = new BeanBuilder()
        bb.beans { auditRequestResolver(TestAuditRequestResolver) }
        bb.registerBeans(grailsApplication.mainContext)

        when:
        String uri = entity.getLogURI()
        String actor = entity.getLogCurrentUserName()

        then:
        uri == "http://foo.com"
        actor == "Aaron"
    }

    /*
    @Ignore
    void "composite ids are handled correctly"() {
        when:
        Author author = new Author(name: "Aaron", age: 37, famous: true).save(flush:true)
        CompositeId compositeId = new CompositeId(
          author: author,
          string: "string",
          nonAuditableCompositeId: new NonAuditableCompositeId(foo:"foo", bar:"bar")
        )

        then:
        compositeId.logEntityId == "[author:$author.id, string:string, nonAuditableCompositeId:toString_for_non_auditable_foo_bar]"
    }*/

    void "test nested withConfig"() {
        when:
        Boolean isDisabled = null
        AuditLogContext.withoutAuditLog {
            AuditLogContext.withoutAuditLog {
                // no-op
            }
            isDisabled = AuditLogContext.context.disabled
        }

        then:
        isDisabled
    }
}



class TestAuditRequestResolver implements AuditRequestResolver {
    @Override
    String getCurrentActor() {
        "Aaron"
    }

    @Override
    String getCurrentURI() {
        "http://foo.com"
    }
}
