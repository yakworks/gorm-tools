package gorm.tools.query

import groovy.transform.CompileStatic

@CompileStatic
public class ListParseUtil {
	/** Accepts a comma separated list as a string, and converts it for safe use as a single-quoted comma separated list.
	 * The intent is to convert anything that might be typed into a parameter table by a user into something that we can
	 * use in a select.  It corrects most of the ham-handedness we have seen in the past.
	 * This method is not compatible with values that are supposed to begin or end in a space.  It assumes all such things
	 * are unintential errors and removes them.  However spaces in the middle of an individual value are preserved.
	 */
	static String sanitizeNameListForSql(String orig) {
		if (orig == null) return null
		String noQuotes = orig.replaceAll(/["']/, '').replaceAll("'", '').trim()
		String noSpaces = noQuotes.replaceAll(', *', ',').replaceAll(' *,', ',') // removes "outer" spaces, not inner
		String noLeadingTrailingCommas = noSpaces.replaceAll('^,*', '').replaceAll(',*$', '')
		String innerQuotes = noLeadingTrailingCommas.replaceAll(',', "','")
		return "'${innerQuotes}'"
	}

	static List<Long> parseLongList(String orig) {
		List<String> strings = parseStringList(orig)
		List<Long> longs = []
		strings.each { longs.add it.toLong() }
		return longs
	}

	static List<String> parseStringList(String orig) {
		if (orig == null) return null
		String noQuotes = orig.replaceAll(/["']/, '').replaceAll("'", '').trim()
		String noSpaces = noQuotes.replaceAll(', *', ',').replaceAll(' *,', ',') // removes "outer" spaces, not inner
		String noLeadingTrailingCommas = noSpaces.replaceAll('^,*', '').replaceAll(',*$', '')
		return noLeadingTrailingCommas.split(',') as List<String>
	}
}