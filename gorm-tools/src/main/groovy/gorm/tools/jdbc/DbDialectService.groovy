/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.jdbc

import java.sql.SQLException
import javax.annotation.PostConstruct

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.grails.datastore.mapping.reflect.ClassUtils

import yakworks.spring.SpringEnvironment

/**
 * Utility class to help create generic code and SQL that can work across supported databases.
 */
@SuppressWarnings(['AssignmentToStaticFieldFromInstanceMethod'])
@Slf4j
@CompileStatic
class DbDialectService implements SpringEnvironment{

    static final int UNKNOWN = 0
    static final int MSSQL = 1
    static final int MYSQL = 2
    static final int ORACLE = 3
    static final int H2 = 4
    static final int POSTGRESQL = 5

    static String hibernateDialect

    /** dialect identity, use init or setHibernateDialect*/
    static int dialect = 0

    @PostConstruct
    void init() {
        def hibDialect = environment.getProperty('hibernate.dialect')
        //dont overwrite it if its null in case we already initialized with static
        if(hibDialect) hibernateDialect = hibDialect
        // init if dialect is not setup yet
        if(!dialect) init(hibernateDialect)
    }

    /**
     * converts a hibernate dialiect name into the static database ids we use.
     * @return 0-5 to map to DbDialectService.H2, DbDialectService.MSSQL etc....
     */
    static int init(String hibernateDialectName) {

        if(!hibernateDialectName){
            //if no hibernateDialectName then set default to H2
            hibernateDialectName = "H2"
            //if no h2 on classpath then hibernate is miconfiged, show error for user.
            if(! ClassUtils.isPresent("org.h2.Driver", DbDialectService.classLoader)){
                log.error("no hibernateDialect set, defaulting to H2 but org.h2.Driver doesnt appear on the classpath.")
            }
        }

        DbDialectService.hibernateDialect = hibernateDialectName
        int result = UNKNOWN

        //fallback to H2 just like how Datasources plugin does. if H2 is present in classpath
        if (hibernateDialectName.contains('H2')) result = H2
        else if (hibernateDialectName.contains("SQLServerDialect")) result = MSSQL
        else if (hibernateDialectName.matches(".*SQLServer20\\d\\dDialect")) result = MSSQL
        else if (hibernateDialectName.contains("MySQL5InnoDBDialect")) result = MYSQL
        else if (hibernateDialectName.contains("Oracle")) result = ORACLE
        else if (hibernateDialectName.contains("Postgre")) result = POSTGRESQL

        if (result == UNKNOWN) {
            throw new SQLException("Unknown dialect ${hibernateDialectName} in gorm.tools.jdbc.DbDialectService.\n"
                    + "Please specify a known for for config hibernate.dialect")
        }

        dialect = result
        return dialect
    }

    String getCurrentDate() {
        String date
        switch (dialect) {
            case MSSQL: date = "getdate()"; break
            case MYSQL: date = "now()"; break
            case ORACLE: date = "SYSDATE"; break
            case H2: date = "CURRENT_DATE()"; break
            case POSTGRESQL: date = "now()"; break
            default: date = "now()"
        }
        date
    }

    String getIfNull() {
        String ifnull
        switch (dialect) {
            case MSSQL: ifnull = "isnull"; break
            case MYSQL: ifnull = "ifnull"; break
            case ORACLE: ifnull = "NVL"; break
            case POSTGRESQL: ifnull = "COALESCE"; break
            default: ifnull = "ifnull"
        }
        ifnull
    }

    //concatenation operater
    String getConcat() {
        String concat
        switch (dialect) {
            case MSSQL: concat = "+"; break
            case MYSQL: concat = "+"; break
            case ORACLE: concat = "||"; break
            case H2: concat = "||"; break
            case POSTGRESQL: concat = "||"; break
            default: concat = "+"
        }
        concat
    }
    //CHAR/CHR Function
    String getCharFn() {
        String charFn
        switch (dialect) {
            case MSSQL: charFn = "CHAR"; break
            case MYSQL: charFn = "CHAR"; break
            case ORACLE: charFn = "CHR"; break
            case POSTGRESQL: charFn = "CHAR"; break
            default: charFn = "CHAR"
        }
        charFn
    }

    //SUBSTRING Function
    String getSubstringFn() {
        String substringFn
        switch (dialect) {
            case MSSQL: substringFn = "SUBSTRING"; break
            case MYSQL: substringFn = "SUBSTRING"; break
            case ORACLE: substringFn = "SUBSTR"; break
            case POSTGRESQL: substringFn = "SUBSTRING"; break
            default: substringFn = "SUBSTRING"
        }
        substringFn
    }

    /**
    * returns the datadiff function with what is passed is. if upperDate is less than lowerDate then result
    * after being run in sql will be negative
    */
    String datediff(Object upperDate, Object lowerDate) {
        String func
        switch (dialect) {
            case MSSQL: func = "DATEDIFF(dd, ${lowerDate}, ${upperDate})"; break
            case MYSQL: func = "DATEDIFF(${upperDate}, ${lowerDate})"; break
            case POSTGRESQL: func = "date_part('day', ${upperDate} - ${lowerDate} )"; break
        }
        func
    }

    static String getDialectName() {
        return getSimpleDialectName(dialect)
    }

    String getTop(int num) {
        String top
        switch (dialect) {
            case MSSQL: top = "TOP ${num}"; break
            case MYSQL: top = "LIMIT ${num}"; break
            case ORACLE: top = "ROWNUM <=${num}"; break
            case POSTGRESQL: top = "fetch first ${num} rows only"; break
            default: top = "LIMIT ${num}"
        }
        top
    }


    void updateOracleDateFormat() {
        if (dialect == ORACLE) {
            String alterOrDateFormat = "alter session set nls_date_format = 'YYYY-MM-dd hh24:mi:ss'"
            //if we decide to support oracle again.
            // jdbcTemplate.update(alterOrDateFormat)
        }
    }

    /** hack for Oracle date formats **/
    @CompileDynamic
    String getDateFormatForDialect(Object myDate) {
        if (getDialect() == ORACLE) {
            Date dateobj
            if (myDate instanceof String) {
                dateobj = new Date(myDate)
            } else {
                dateobj = myDate
            }
            String formattedDate = dateobj.format("yyyy-MM-dd hh:mm:ss")
            return " to_date (\' $formattedDate \', \'YYYY-MM-dd hh24:mi:ss\')"
        }
        //do nothing for all the others
        return myDate
    }

    static String getSimpleDialectName(int dialectKey) {
        String simpleName
        switch (dialectKey) {
            case MSSQL: simpleName = "mssql"; break
            case MYSQL: simpleName = "mysql"; break
            case ORACLE: simpleName = "oracle"; break
            case H2: simpleName = "h2"; break
            case POSTGRESQL: simpleName = "postgresql"; break
            default: simpleName = "mysql"
        }
        simpleName
    }

    //used to inject confugrationProperties into ibatis SqlSessionFactoryBean
    static Map getGlobalVariables() {
        return [dialect: getDialectName()]
    }

    boolean isMySql() {
        return dialect == MYSQL
    }

    boolean isMsSql() {
        return dialect == MSSQL
    }

    boolean isH2() {
        return dialect == H2
    }

    boolean isPostgres() {
        return dialect == POSTGRESQL
    }

}
