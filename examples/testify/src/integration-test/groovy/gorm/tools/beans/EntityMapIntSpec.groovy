package gorm.tools.beans

import gorm.tools.metamap.MetaMapEntityService
import gorm.tools.security.domain.AppUser
import yakworks.gorm.testing.SecuritySpecHelper
import gorm.tools.testing.integration.DataIntegrationTest
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Ignore
import spock.lang.Specification

@Integration
@Rollback
class EntityMapIntSpec extends Specification implements DataIntegrationTest, SecuritySpecHelper {

    MetaMapEntityService metaMapEntityService

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
        def emap = metaMapEntityService.createMetaMap(user, ['username', 'roles.id', 'roles.name'])

        then:
        emap['roles'] == [ [id:1, name:'Administrator'] , [id:2, name:'Power User']]

    }

    void "test no roles user"() {
        when:
        def user = AppUser.get(2)
        assert user.roles.size() == 0
        // def emap = EntityMapFactory.createEntityMap(user, ['username', 'stringList'])
        def emap = metaMapEntityService.createMetaMap(user, ['username', 'roles.id', 'roles.name'])

        then:
        emap['roles'] == []

    }
}
