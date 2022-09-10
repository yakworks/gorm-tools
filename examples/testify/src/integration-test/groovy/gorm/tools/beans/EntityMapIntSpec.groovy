package gorm.tools.beans


import gorm.tools.metamap.services.MetaMapService
import gorm.tools.security.domain.AppUser
import yakworks.gorm.testing.integration.DataIntegrationTest
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.gorm.testing.SecuritySpecHelper

@Integration
@Rollback
class EntityMapIntSpec extends Specification implements DataIntegrationTest, SecuritySpecHelper {

    MetaMapService metaMapService

    @Ignore //playground
    void "MetaBeanProperty playground"() {
        when:
        def user = AppUser.get(1)

        then:
        user
        user.metaClass.properties.each{
            MetaBeanProperty mbp = it as MetaBeanProperty
            if(mbp.name == 'roles') {
                println "it.name : ${mbp.name} ${mbp}"
                println "it.getter.returnType : ${mbp.getter.returnType}"
                println "Collection.isAssignableFrom : ${Collection.isAssignableFrom(mbp.getter.returnType)}"
                // println "it.field.genericType calss : ${it.field?.field?.genericType.class}"
                // println "it.field.genericType : ${it.field?.field?.genericType}"
                // //ParameterizedType
                // println "it.field.genericType.actualTypeArguments : ${it.field?.field?.genericType.actualTypeArguments}"
                println "it.getter class : ${it.getter.class}"
                println "it.getter genericReturnType.actualTypeArguments : ${it.getter.cachedMethod.genericReturnType.actualTypeArguments[0].typeName}"

                println "it.getter : ${it.getter.toString()}"
            }
            // println "it.name : ${it.name} ${it}"
            // println "it.field.genericType : ${it.field?.field?.genericType}"
        }
        // println "list : ${dataTypes(user.class)}"
        // List<String> generics = user.metaClass.metaClass.properties.findAll {MetaProperty metaProperty ->
        //         metaProperty?.field != null
        //     }.collect {findGenerics(it.type)}
        //
        //     c.metaClass.properties.each {MetaProperty mp ->
        //     println "name: ${mp.name}"
        //     println "name: ${mp.name}"
        // }

    }

    void "test AppUser"() {
        when:
        def user = AppUser.get(1)
        assert user.roles.size() == 2
        // def emap = EntityMapFactory.createEntityMap(user, ['username', 'stringList'])
        def emap = metaMapService.createMetaMap(user, ['username', 'roles.id', 'roles.code'])

        then:
        emap['roles'] == [ [id:1, code:'ADMIN'] , [id:2, code:'POWER_USER']]

    }

    void "test no roles user"() {
        when:
        def user = AppUser.get(3)
        assert user.roles.size() == 0
        // def emap = EntityMapFactory.createEntityMap(user, ['username', 'stringList'])
        def emap = metaMapService.createMetaMap(user, ['username', 'roles.id', 'roles.name'])

        then:
        emap['roles'] == []

    }
}
