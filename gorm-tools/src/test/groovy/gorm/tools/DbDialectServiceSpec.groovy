/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools

import gorm.tools.jdbc.DbDialectService
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class DbDialectServiceSpec extends Specification implements ServiceUnitTest<DbDialectService> {

    void setup() {
        service.dialectName = null
    }

    void "test getDialect"() {
        when: "dialect is null"
        config.hibernate.dialect = null
        int dialect = service.getDialect()

        then:
        dialect == DbDialectService.H2
        service.dialectName == "dialect_h2"
        service.isH2() == true

        when: "mysql"
        config.hibernate.dialect = 'MySQL5InnoDBDialect'
        dialect = service.getDialect()

        then:
        dialect == DbDialectService.MYSQL
        service.dialectName == "dialect_mysql"
        service.isMySql() == true

        when: "mssql server 20**"
        service.dialectName = null
        config.hibernate.dialect = 'testSQLServer2012Dialect'
        dialect = service.getDialect()

        then:
        dialect == DbDialectService.MSSQL
        service.dialectName == "dialect_mssql"
        service.isMsSql() == true

        when: "sql server"
        service.dialectName = null
        config.hibernate.dialect = 'SQLServerDialect'
        dialect = service.getDialect()

        then:
        dialect == DbDialectService.MSSQL
        service.dialectName == "dialect_mssql"
        service.isMsSql() == true

        when: "postgresql"
        service.dialectName = null
        config.hibernate.dialect = 'PostgreSQLDialect'
        dialect = service.getDialect()

        then:
        dialect == DbDialectService.POSTGRES
        service.dialectName == "dialect_postgres"
        service.isPostgres() == true
    }

    void "test getCurrentDate"() {
        when: "dialect is H2"
        config.hibernate.dialect = 'H2Dialect'
        String date = service.currentDate

        then:
        date == "CURRENT_DATE()"

        when: "mysql"
        service.dialectName = null
        config.hibernate.dialect = 'MySQL5InnoDBDialect'
        date = service.currentDate

        then:
        date == "now()"

        when: "sql server"
        service.dialectName = null
        config.hibernate.dialect = 'SQLServerDialect'
        date = service.currentDate

        then:
        date == "getdate()"

        when: "postgresql"
        service.dialectName = null
        config.hibernate.dialect = "PostgreSQLDialect"
        date = service.currentDate

        then:
        date == "now()"
    }

    void "test getIfNull"() {
        when: "dialect is H2"
        config.hibernate.dialect = 'H2Dialect'
        String function = service.ifNull

        then:
        function == "ifnull"

        when: "mysql"
        service.dialectName = null
        config.hibernate.dialect = 'MySQL5InnoDBDialect'
        function = service.ifNull

        then:
        function == "ifnull"

        when: "sql server"
        service.dialectName = null
        config.hibernate.dialect = 'SQLServerDialect'
        function = service.ifNull

        then:
        function == "isnull"

        when: "postgresql"
        service.dialectName = null
        config.hibernate.dialect = "PostgreSQLDialect"
        function = service.ifNull

        then:
        function == 'is null'
    }

    void "test getConcat"() {
        when: "dialect is H2"
        config.hibernate.dialect = 'H2Dialect'
        String function = service.concat

        then:
        function == "||"

        when: "mysql"
        service.dialectName = null
        config.hibernate.dialect = 'MySQL5InnoDBDialect'
        function = service.concat

        then:
        function == "+"

        when: "sql server"
        service.dialectName = null
        config.hibernate.dialect = 'SQLServerDialect'
        function = service.concat

        then:
        function == "+"

        when: "postgresql"
        service.dialectName = null
        config.hibernate.dialect = "PostgreSQLDialect"
        function = service.concat

        then:
        function == "||"
    }


    void "test getCharFn"() {
        when: "dialect is H2"
        config.hibernate.dialect = 'H2Dialect'
        String function = service.charFn

        then:
        function == "CHAR"

        when: "mysql"
        service.dialectName = null
        config.hibernate.dialect = 'MySQL5InnoDBDialect'
        function = service.charFn

        then:
        function == "CHAR"

        when: "sql server"
        service.dialectName = null
        config.hibernate.dialect = 'SQLServerDialect'
        function = service.charFn

        then:
        function == "CHAR"

        when: "Oracle"
        service.dialectName = null
        config.hibernate.dialect = 'OracleDailect'
        function = service.charFn

        then:
        function == "CHR"

        when: "postgresql"
        service.dialectName = null
        config.hibernate.dialect = "PostgreSQLDialect"
        function = service.charFn

        then:
        function == "CHAR"
    }


    void "test getSubstringFn"() {
        when: "dialect is H2"
        config.hibernate.dialect = 'H2Dialect'
        String function = service.substringFn

        then:
        function == "SUBSTRING"

        when: "mysql"
        service.dialectName = null
        config.hibernate.dialect = 'MySQL5InnoDBDialect'
        function = service.substringFn

        then:
        function == "SUBSTRING"

        when: "sql server"
        service.dialectName = null
        config.hibernate.dialect = 'SQLServerDialect'
        function = service.substringFn

        then:
        function == "SUBSTRING"

        when: "Oracle"
        service.dialectName = null
        config.hibernate.dialect = 'OracleDailect'
        function = service.substringFn

        then:
        function == "SUBSTR"

        when: "postgresql"
        service.dialectName = null
        config.hibernate.dialect = "PostgreSQLDialect"
        function = service.substringFn

        then:
        function == "SUBSTRING"
    }


    void "test getGlobalVariables"() {
        when: "dialect is H2"
        config.hibernate.dialect = 'H2Dialect'
        String function = DbDialectService.globalVariables.concat

        then:
        function == "FN9_CONCAT"

        when: "mysql"
        service.dialectName = null
        config.hibernate.dialect = 'MySQL5InnoDBDialect'
        function = DbDialectService.globalVariables.concat

        then:
        function == "FN9_CONCAT"

        when: "sql server"
        service.dialectName = null
        config.hibernate.dialect = 'SQLServerDialect'
        function = DbDialectService.globalVariables.concat

        then:
        function == "dbo.FN9_CONCAT"

        when: "Oracle"
        service.dialectName = null
        config.hibernate.dialect = 'OracleDailect'
        function = DbDialectService.globalVariables.concat

        then:
        function == "FN9_CONCAT"

        when: "postgresql"
        service.dialectName = null
        config.hibernate.dialect = "PostgreSQLDialect"
        function = DbDialectService.globalVariables.concat

        then:
        function == "FN9_CONCAT"
    }

}
