/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.databinding

import java.util.regex.Matcher
import java.util.regex.Pattern

import groovy.transform.CompileStatic

import org.springframework.util.StringUtils

/**
 * Copied in from older spring boot 1.5
 * Generates relaxed name variations from a given source for the RelaxedConversionService
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @since 1.0.0
 */
@SuppressWarnings(['CompileStatic', 'ConfusingTernary'])
@CompileStatic
class RelaxedNames implements Iterable<String> {

    public static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("([^A-Z-])([A-Z])");

    public static final Pattern SEPARATED_TO_CAMEL_CASE_PATTERN = Pattern.compile("[_\\-.]");

    private final String name;

    private final Set<String> values = new LinkedHashSet<String>();

    /**
     * Create a new {@link RelaxedNames} instance.
     * @param name the source name. For the maximum number of variations specify the name
     * using dashed notation (e.g. {@literal my-property-name}
     */
    public RelaxedNames(String name) {
        this.name = (name != null) ? name : "";
        initialize(this.name, this.values);
    }

    @Override
    public Iterator<String> iterator() {
        return this.values.iterator();
    }

    private void initialize(String name, Set<String> values) {
        if (values.contains(name)) {
            return;
        }
        for (Variation variation : Variation.values()) {
            for (Manipulation manipulation : Manipulation.values()) {
                String result = name;
                result = manipulation.apply(result);
                result = variation.apply(result);
                values.add(result);
                initialize(result, values);
            }
        }
    }

    /**
     * Name variations.
     */
    enum Variation {

        NONE {

            @Override
            public String apply(String value) {
                return value;
            }

        },

        LOWERCASE {

            @Override
            public String apply(String value) {
                return (value.isEmpty() ? value : value.toLowerCase(Locale.ENGLISH));
            }

        },

        UPPERCASE {

            @Override
            public String apply(String value) {
                return (value.isEmpty() ? value : value.toUpperCase(Locale.ENGLISH));
            }

        };

        public abstract String apply(String value);

    }

    /**
     * Name manipulations.
     */
    enum Manipulation {

        NONE {

            @Override
            public String apply(String value) {
                return value;
            }

        },

        HYPHEN_TO_UNDERSCORE {

            @Override
            public String apply(String value) {
                return (value.indexOf('-') != -1) ? value.replace('-', '_') : value;
            }

        },

        UNDERSCORE_TO_PERIOD {

            @Override
            public String apply(String value) {
                return (value.indexOf('_') != -1) ? value.replace('_', '.') : value;
            }

        },

        PERIOD_TO_UNDERSCORE {

            @Override
            public String apply(String value) {
                return (value.indexOf('.') != -1) ? value.replace('.', '_') : value;
            }

        },

        CAMELCASE_TO_UNDERSCORE {

            @Override
            public String apply(String value) {
                if (value.isEmpty()) {
                    return value;
                }
                Matcher matcher = RelaxedNames.CAMEL_CASE_PATTERN.matcher(value);
                if (!matcher.find()) {
                    return value;
                }
                matcher = matcher.reset();
                StringBuffer result = new StringBuffer();
                while (matcher.find()) {
                    matcher.appendReplacement(result,
                        matcher.group(1) + '_' + StringUtils.uncapitalize(matcher.group(2)));
                }
                matcher.appendTail(result);
                return result.toString();
            }

        },

        CAMELCASE_TO_HYPHEN {

            @Override
            public String apply(String value) {
                if (value.isEmpty()) {
                    return value;
                }
                Matcher matcher = RelaxedNames.CAMEL_CASE_PATTERN.matcher(value);
                if (!matcher.find()) {
                    return value;
                }
                matcher = matcher.reset();
                StringBuffer result = new StringBuffer();
                while (matcher.find()) {
                    matcher.appendReplacement(result,
                        matcher.group(1) + '-' + StringUtils.uncapitalize(matcher.group(2)));
                }
                matcher.appendTail(result);
                return result.toString();
            }

        },

        SEPARATED_TO_CAMELCASE {

            @Override
            public String apply(String value) {
                return separatedToCamelCase(value, false);
            }

        },

        CASE_INSENSITIVE_SEPARATED_TO_CAMELCASE {

            @Override
            public String apply(String value) {
                return separatedToCamelCase(value, true);
            }

        };

        private static final char[] SUFFIXES = [ '_', '-', '.' ] as char[]

        public abstract String apply(String value);

        private static String separatedToCamelCase(String value, boolean caseInsensitive) {
            if (value.isEmpty()) {
                return value;
            }
            StringBuilder builder = new StringBuilder();
            for (String field : SEPARATED_TO_CAMEL_CASE_PATTERN.split(value)) {
                field = (caseInsensitive ? field.toLowerCase(Locale.ENGLISH) : field);
                builder.append((builder.length() != 0) ? StringUtils.capitalize(field) : field);
            }
            char lastChar = value.charAt(value.length() - 1);
            for (char suffix : SUFFIXES) {
                if (lastChar == suffix) {
                    builder.append(suffix);
                    break;
                }
            }
            return builder.toString();
        }

    }

    /**
     * Return a {@link RelaxedNames} for the given source camelCase source name.
     * @param name the source name in camelCase
     * @return the relaxed names
     */
    public static RelaxedNames forCamelCase(String name) {
        StringBuilder result = new StringBuilder();
        for (char c : name.toCharArray()) {
            result.append((Character.isUpperCase(c) && result.length() > 0 && result.charAt(result.length() - 1) != '-')
                ? "-" + Character.toLowerCase(c) : c);
        }
        return new RelaxedNames(result.toString());
    }

}