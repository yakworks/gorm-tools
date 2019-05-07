/* Copyright 2018. 9ci Inc. Licensed under the Apache License, Version 2.0 */
package gorm.tools

import gorm.tools.beans.AppCtx
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.util.ClassUtils

import java.sql.SQLException

/**
 * Utility class to help create generic code and SQL that can work across supported databases.
 */
@CompileStatic
class DbDialectService {

    static final int UNKNOWN = 0
    static final int MSSQL = 1
    static final int MYSQL = 2
    static final int ORACLE = 3
    static final int H2 = 4

    //GrailsApplication grailsApplication
    JdbcTemplate jdbcTemplate

    static String dialectName

    //need this static so that getGlobalVariables can be accessed from doWithSpring in rally plugin
    @CompileDynamic
    private static int setupDialect() {
        int result = UNKNOWN
        // just to make the stuff below easier to read.
        if (!dialectName) dialectName = AppCtx.config.hibernate.dialect

        //fallback to H2 just like how Datasources plugin does. if H2 is present in classpath
        if ((dialectName == null && ClassUtils.isPresent("org.h2.Driver", getClass().classLoader))
            || dialectName.contains('H2')) result = H2
        else if (dialectName.contains("SQLServerDialect")) result = MSSQL
        else if (dialectName.contains("MySQL5InnoDBDialect")) result = MYSQL
        else if (dialectName.contains("Oracle")) result = ORACLE

        if (result == UNKNOWN) {
            throw new SQLException("Unknown dialect ${dialectName} in nine.rally.DbDialectService.\n"
                    + "Please use a known dialect or make accommodation for a new dialect.")
        }

        return result
    }

    int getDialect() {
        return setupDialect()
    }

    String getCurrentDate() {
        String date
        switch (dialect) {
            case MSSQL: date = "getdate()"; break
            case MYSQL: date = "now()"; break
            case ORACLE: date = "SYSDATE"; break
            case H2: date = "CURRENT_DATE()"; break
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
            default: substringFn = "SUBSTRING"
        }
        substringFn
    }

    String getDialectName() {
        String dialectName
        switch (dialect) {
            case MSSQL: dialectName = "dialect_mssql"; break
            case MYSQL: dialectName = "dialect_mysql"; break
            case ORACLE: dialectName = "dialect_oracle"; break
            case H2: dialectName = "dialect_h2"; break
            default: dialectName = "dialect_mysql"
        }
        dialectName
    }

    String getTop(int num) {
        String top
        switch (dialect) {
            case MSSQL: top = "TOP ${num}"; break
            case MYSQL: top = "LIMIT ${num}"; break
            case ORACLE: top = "ROWNUM <=${num}"; break
            default: top = "LIMIT ${num}"
        }
        top
    }

    void updateOrDateFormat() {
        if (dialect == ORACLE) {
            String alterOrDateFormat = "alter session set nls_date_format = 'YYYY-MM-dd hh24:mi:ss'"

            jdbcTemplate.update(alterOrDateFormat)
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

    static Map getGlobalVariables() {
        Map result = [:]
        int dialect = setupDialect()
        if (dialect == MYSQL || dialect == H2 || dialect == ORACLE) {
            result.concat = "FN9_CONCAT"
        } else if (dialect == MSSQL) {
            result.concat = "dbo.FN9_CONCAT"
        }

        return result
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

}
