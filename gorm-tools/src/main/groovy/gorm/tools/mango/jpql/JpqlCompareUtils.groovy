package gorm.tools.mango.jpql

import org.hibernate.engine.jdbc.internal.BasicFormatterImpl

/**
 * Helpers to compare
 */
class JpqlCompareUtils {

    // static String strip(String val){
    //     val = val.stripIndent().replace('\n',' ').trim()
    //     new BasicFormatterImpl().format(val)
    // }

    static String sqlFormat(String q){
        new BasicFormatterImpl().format(q)
    }

    static String formatAndStrip(String q){
        noNewLines(sqlFormat(q))
    }

    /**
     * trims each line and then joins without line feeds to make easier to compare
     */
    static String noNewLines(String q){
        q.readLines().collect {it.trim() }.join(' ')
    }

    /**
     * uses hibernated SQL to format and compares 2 strings
     * NOTE: copy this into test class WITHOUT static and spock will show you what part of the string doesn't match
     */
    static boolean compareQuery(String hql, String expected){
        assert formatAndStrip(hql) == formatAndStrip(expected.stripIndent())
        return true
    }

}
