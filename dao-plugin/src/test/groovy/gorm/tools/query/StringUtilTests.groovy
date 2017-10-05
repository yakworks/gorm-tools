package gorm.tools.query

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class ListParseUtilTests extends Specification {

	void testSanitizeNameListForSql() {
		expect:
		"'a','b','c'" == ListParseUtil.sanitizeNameListForSql("a,b,c")
		"'a','b','c'" == ListParseUtil.sanitizeNameListForSql("a,b,c,")
		"'a','b','c'" == ListParseUtil.sanitizeNameListForSql(",a,b,c")
		"'a','b','c'" == ListParseUtil.sanitizeNameListForSql(",a,b,c,")
		"'a','b','c'" == ListParseUtil.sanitizeNameListForSql(" a , b , c ")
		"'a','b','c'" == ListParseUtil.sanitizeNameListForSql("'a','b','c'")
		"'a b','b c','c d'" == ListParseUtil.sanitizeNameListForSql("a b,b c,c d")
	}

	void testParseLongList() {
		given:
		def RESULT = [1, 2, 3, 4]

		expect:
		RESULT == ListParseUtil.parseLongList('1,2,3,4')
		RESULT == ListParseUtil.parseLongList('1 ,2, 3, 4')
		RESULT == ListParseUtil.parseLongList(',1,2,3,4,')
		RESULT != ListParseUtil.parseLongList('4,3,2,1')
	}

}
