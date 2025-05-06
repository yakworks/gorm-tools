package yakworks.hazel


import com.hazelcast.internal.serialization.SerializationService
import com.hazelcast.internal.serialization.impl.DefaultSerializationServiceBuilder
import gorm.tools.beans.Pager
import spock.lang.Specification

/**
 * Serialization smoke test
 */
class HazelSerialSpec extends Specification {

    void "test serial"() {
        when:
        SerializationService serializationService = new DefaultSerializationServiceBuilder().build();

        Map map = [foo: 123]
        var mapSerial = serializationService.toData(map)
        var deserialMap = serializationService.toObject(mapSerial)

        then:
        map == deserialMap

    }

    void "test pager serial"() {
        when:
        SerializationService serializationService = new DefaultSerializationServiceBuilder().build();

        Pager obj = Pager.of([page: 1, max: 20])
        var objSerial = serializationService.toData(obj)
        var deserialObj = serializationService.toObject(objSerial)

        then:
        obj == deserialObj

    }
}
