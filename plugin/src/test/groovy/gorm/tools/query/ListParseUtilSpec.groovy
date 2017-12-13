package gorm.tools.query

import spock.lang.Specification

class ListParseUtilSpec extends Specification {

    void "test sanitizeNameListForSql"() {
        expect:
        "'a','b','c'" == ListParseUtil.sanitizeNameListForSql("a,b,c")
        "'a','b','c'" == ListParseUtil.sanitizeNameListForSql("a,b,c,")
        "'a','b','c'" == ListParseUtil.sanitizeNameListForSql(",a,b,c")
        "'a','b','c'" == ListParseUtil.sanitizeNameListForSql(",a,b,c,")
        "'a','b','c'" == ListParseUtil.sanitizeNameListForSql(" a , b , c ")
        "'a','b','c'" == ListParseUtil.sanitizeNameListForSql("'a','b','c'")
        "'a b','b c','c d'" == ListParseUtil.sanitizeNameListForSql("a b,b c,c d")
    }

    void "test parseLongList"() {
        given:
        List expectedResult = [1, 2, 3, 4]

        expect:
        expectedResult == ListParseUtil.parseLongList('1,2,3,4')
        expectedResult == ListParseUtil.parseLongList('1 ,2, 3, 4')
        expectedResult == ListParseUtil.parseLongList(',1,2,3,4,')
        expectedResult != ListParseUtil.parseLongList('4,3,2,1')
    }

    void "test ParseStringList"() {
        given:
        List expectedResult = ['aaa', 'bbb', 'ccc']

        expect:
        expectedResult == ListParseUtil.parseStringList("'aaa',\"bbb\",'ccc'")
        expectedResult == ListParseUtil.parseStringList("aaa, bbb, ccc")
        expectedResult == ListParseUtil.parseStringList("'aaa', 'bbb' ,' ccc'")
        expectedResult == ListParseUtil.parseStringList(" 'aaa' ,     'bbb' ,' ccc'")
        expectedResult != ListParseUtil.parseStringList(" 'aaa' ,     'bbb' ,' ccd'")
        expectedResult != ListParseUtil.parseStringList(" 'aaa' ,  ' ccc',   'bbb'")
    }

}
