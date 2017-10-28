package gorm.tools.query

import groovy.transform.CompileStatic

/**
 * ListParseUtil provides a set of static helpers for parsing and manipulating with comma separated lists in strings.
 */
@CompileStatic
class ListParseUtil {

    /**
     * Accepts a comma separated list as a string, and converts it for safe use as a single-quoted comma separated list.
     * The intent is to convert anything that might be typed into a parameter table by a user into something that we can
     * use in a select. It corrects most of the ham-handedness we have seen in the past.
     * This method is not compatible with values that are supposed to begin or end in a space. It assumes all such things
     * are unintentional errors and removes them. However spaces in the middle of an individual value are preserved.
     *
     * @param orig a string with a comma separated list
     * @return normalized list as a string
     */
    static String sanitizeNameListForSql(String orig) {
        if (orig == null) return null
        String noQuotes = orig.replaceAll(/["']/, '').replaceAll("'", '').trim()
        String noSpaces = noQuotes.replaceAll(', *', ',').replaceAll(' *,', ',') // removes "outer" spaces, not inner
        String noLeadingTrailingCommas = noSpaces.replaceAll('^,*', '').replaceAll(',*$', '')
        String innerQuotes = noLeadingTrailingCommas.replaceAll(',', "','")
        return "'${innerQuotes}'"
    }

    /**
     * Parses a comma separated list of numbers in a given string.
     *
     * @param orig a comma separated list of numbers
     * @return a list of Long values or empty list in case the given string is empty or null
     */
    static List<Long> parseLongList(String orig) {
        List<String> strings = parseStringList(orig)
        List<Long> longs = []
        strings.each { longs.add it.toLong() }
        return longs
    }

    /**
     * Parses a comma separated list of strings in a given String instance into an actual list of strings.
     *
     * @param orig a comma separated list of strings
     * @return a list of String which represents a comma separated list or empty list
     *         in case the given string is empty or null
     */
    static List<String> parseStringList(String orig) {
        if (!orig) return []
        String noQuotes = orig.replaceAll(/["']/, '').replaceAll("'", '').trim()
        String noSpaces = noQuotes.replaceAll(', *', ',').replaceAll(' *,', ',') // removes "outer" spaces, not inner
        String noLeadingTrailingCommas = noSpaces.replaceAll('^,*', '').replaceAll(',*$', '')
        return noLeadingTrailingCommas.split(',') as List<String>
    }
}