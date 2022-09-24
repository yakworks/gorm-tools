/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools

import gorm.tools.jdbc.DbDialectService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import testing.Cust
import yakworks.testing.gorm.unit.GormHibernateTest

class DbDialectServiceSpec extends Specification implements GormHibernateTest {

    //dbDialectService is already one of the beans setup in GormHibernateTest so just inject it
    @Autowired DbDialectService dbDialectService

    //test a bunch of ways to setup dialect
    void "test initFromHibernateDialect"() {
        when: "dialect is null"
        dbDialectService.init("")
        int dialect = dbDialectService.dialect

        then:
        dialect == DbDialectService.H2
        dbDialectService.dialectName == "h2"
        dbDialectService.isH2() == true

        when: "mysql"
        DbDialectService.init('MySQL5InnoDBDialect')
        dialect = dbDialectService.getDialect()

        then:
        dialect == DbDialectService.MYSQL
        dbDialectService.dialectName == "mysql"
        dbDialectService.isMySql() == true

        when: "mssql server 20**"
        DbDialectService.init('testSQLServer2012Dialect')
        dialect = dbDialectService.getDialect()

        then:
        dialect == DbDialectService.MSSQL
        dbDialectService.dialectName == "mssql"
        dbDialectService.isMsSql() == true

        when: "sql server"
        DbDialectService.init('SQLServerDialect')
        dialect = dbDialectService.getDialect()

        then:
        dialect == DbDialectService.MSSQL
        dbDialectService.dialectName == "mssql"
        dbDialectService.isMsSql() == true

        when: "postgresql"
        DbDialectService.init('PostgreSQLDialect')
        dialect = dbDialectService.getDialect()

        then:
        dialect == DbDialectService.POSTGRESQL
        dbDialectService.dialectName == "postgresql"
        dbDialectService.isPostgres() == true
    }

    void "test getCurrentDate"() {
        when: "dialect is H2"
        dbDialectService.dialect = DbDialectService.H2
        String date = dbDialectService.currentDate

        then:
        date == "CURRENT_DATE()"

        when: "mysql"
        dbDialectService.dialect = DbDialectService.MYSQL
        date = dbDialectService.currentDate

        then:
        date == "now()"

        when: "sql server"
        dbDialectService.dialect = DbDialectService.MSSQL
        date = dbDialectService.currentDate

        then:
        date == "getdate()"

        when: "postgresql"
        dbDialectService.dialect = DbDialectService.POSTGRESQL
        date = dbDialectService.currentDate

        then:
        date == "now()"
    }

    void "test getIfNull"() {
        when: "dialect is H2"
        dbDialectService.dialect = DbDialectService.H2
        String function = dbDialectService.ifNull

        then:
        function == "ifnull"

        when: "mysql"
        dbDialectService.dialect = DbDialectService.MYSQL
        function = dbDialectService.ifNull

        then:
        function == "ifnull"

        when: "sql server"
        dbDialectService.dialect = DbDialectService.MSSQL
        function = dbDialectService.ifNull

        then:
        function == "isnull"

        when: "postgresql"
        dbDialectService.dialect = DbDialectService.POSTGRESQL
        function = dbDialectService.ifNull

        then:
        function == 'COALESCE'
    }

    void "test getConcat"() {
        when: "dialect is H2"
        dbDialectService.dialect = DbDialectService.H2
        String function = dbDialectService.concat

        then:
        function == "||"

        when: "mysql"
        dbDialectService.dialect = DbDialectService.MYSQL
        function = dbDialectService.concat

        then:
        function == "+"

        when: "sql server"
        dbDialectService.dialect = DbDialectService.MSSQL
        function = dbDialectService.concat

        then:
        function == "+"

        when: "postgresql"
        DbDialectService.dialect = DbDialectService.POSTGRESQL
        function = dbDialectService.concat

        then:
        function == "||"
    }


    void "test getCharFn"() {
        when: "dialect is H2"
        dbDialectService.dialect = DbDialectService.H2
        String function = dbDialectService.charFn

        then:
        function == "CHAR"

        when: "mysql"
        dbDialectService.dialect = DbDialectService.MYSQL
        function = dbDialectService.charFn

        then:
        function == "CHAR"

        when: "sql server"
        dbDialectService.dialect = DbDialectService.MSSQL
        function = dbDialectService.charFn

        then:
        function == "CHAR"

        when: "postgresql"
        DbDialectService.dialect = DbDialectService.POSTGRESQL
        function = dbDialectService.charFn

        then:
        function == "CHAR"
    }


    void "test getSubstringFn"() {
        when: "dialect is H2"
        dbDialectService.dialect = DbDialectService.H2
        String function = dbDialectService.substringFn

        then:
        function == "SUBSTRING"

        when: "mysql"
        dbDialectService.dialect = DbDialectService.MYSQL
        function = dbDialectService.substringFn

        then:
        function == "SUBSTRING"

        when: "sql server"
        dbDialectService.dialect = DbDialectService.MSSQL
        function = dbDialectService.substringFn

        then:
        function == "SUBSTRING"

        when: "postgresql"
        dbDialectService.dialect = DbDialectService.POSTGRESQL
        function = dbDialectService.substringFn

        then:
        function == "SUBSTRING"
    }

}
