/*
 * Copyright 2004-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gorm.tools.databinding;

import grails.databinding.DataBinder;
import grails.io.IOUtils;
import grails.util.TypeConvertingMap;
import grails.web.mime.MimeType;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.grails.datastore.mapping.model.config.GormProperties;
import org.grails.web.binding.StructuredDateEditor;
import org.grails.web.servlet.mvc.GrailsWebRequest;
import org.grails.web.servlet.mvc.exceptions.ControllerExecutionException;
import org.grails.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a copy of the GrailsParameterMap, primary to remove the need for HttpServletRequest.
 * Allows a flattened map of path keys such that
 * foo.bar.id:1, foo.amount:10 would end up as [foo: [bar: [id: 1]], amount:10]
 *
 * Orginal authors from GrailsParameterMap
 * @author Graeme Rocher
 * @author Lari Hotari
 * @since Oct 24, 2005\
 * TODO convert to groovy
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class PathKeyMap extends TypeConvertingMap implements Cloneable {
    private static final Logger LOG = LoggerFactory.getLogger(PathKeyMap.class);
    public static final Object[] EMPTY_ARGS = new Object[0];

    public String pathDelimiter();
    /**
     * Does not populate the GrailsParameterMap from the request but instead uses the supplied values.
     *
     * @param values The values to populate with
     */
    public PathKeyMap(Map values) {
        wrappedMap.putAll(values);
        updateNestedKeys(values);
    }

    // add class variable that we use it
    public PathKeyMap(Map values, String pathDelimiter) {
        this(values);
        this.pathDelimiter = pathDelimiter;
    }


    @Override
    public Object clone() {
        if (wrappedMap.isEmpty()) {
            return new PathKeyMap(new LinkedHashMap());
        } else {
            Map clonedMap = new LinkedHashMap(wrappedMap);
            // deep clone nested entries
            for(Iterator it=clonedMap.entrySet().iterator();it.hasNext();) {
                Entry entry = (Entry)it.next();
                if (entry.getValue() instanceof PathKeyMap) {
                    entry.setValue(((PathKeyMap)entry.getValue()).clone());
                }
            }
            return new PathKeyMap(clonedMap);
        }
    }

    public void mergeValuesFrom(PathKeyMap otherMap) {
        wrappedMap.putAll((PathKeyMap)otherMap.clone());
    }

    @Override
    public Object get(Object key) {
        // removed test for String key because there
        // should be no limitations on what you shove in or take out
        Object returnValue = null;

        returnValue = wrappedMap.get(key);
        if (returnValue instanceof String[]) {
            String[] valueArray = (String[])returnValue;
            if (valueArray.length == 1) {
                returnValue = valueArray[0];
            } else {
                returnValue = valueArray;
            }
        }
        else if(returnValue == null && (key instanceof Collection)) {
            return DefaultGroovyMethods.subMap(wrappedMap, (Collection)key);
        }

        return returnValue;
    }

    @Override
    public Object put(Object key, Object value) {
        if (value instanceof CharSequence) value = value.toString();
        if (key instanceof CharSequence) key = key.toString();
        Object returnValue =  wrappedMap.put(key, value);
        if (key instanceof String) {
            String keyString = (String)key;
            if (keyString.indexOf(".") > -1) {
                processNestedKeys(this, keyString, keyString, wrappedMap);
            }
        }
        return returnValue;
    }

    @Override
    public Object remove(Object key) {
        return wrappedMap.remove(key);
    }

    @Override
    public void putAll(Map map) {
        for (Object entryObj : map.entrySet()) {
            Entry entry = (Entry)entryObj;
            put(entry.getKey(), entry.getValue());
        }
    }


    /**
     * @return The identifier in the request
     */
    public Object getIdentifier() {
        return get(GormProperties.IDENTITY);
    }

    protected void updateNestedKeys(Map keys) {
        for (Object keyObject : keys.keySet()) {
            String key = (String)keyObject;
            processNestedKeys(keys, key, key, wrappedMap);
        }
    }

    /*
     * Builds up a multi dimensional hash structure from the parameters so that nested keys such as
     * "book.author.name" can be addressed like params['author'].name
     *
     * This also allows data binding to occur for only a subset of the properties in the parameter map.
     */
    private void processNestedKeys(Map requestMap, String key, String nestedKey, Map nestedLevel) {
        final int nestedIndex = nestedKey.indexOf('.');
        if (nestedIndex == -1) {
            return;
        }

        // We have at least one sub-key, so extract the first element
        // of the nested key as the prfix. In other words, if we have
        // 'nestedKey' == "a.b.c", the prefix is "a".
        String nestedPrefix = nestedKey.substring(0, nestedIndex);
        boolean prefixedByUnderscore = false;

        // Use the same prefix even if it starts with an '_'
        if (nestedPrefix.startsWith("_")) {
            prefixedByUnderscore = true;
            nestedPrefix = nestedPrefix.substring(1);
        }
        // Let's see if we already have a value in the current map for the prefix.
        Object prefixValue = nestedLevel.get(nestedPrefix);
        if (prefixValue == null) {
            // No value. So, since there is at least one sub-key,
            // we create a sub-map for this prefix.

            prefixValue = new PathKeyMap(new LinkedHashMap());
            nestedLevel.put(nestedPrefix, prefixValue);
        }

        // If the value against the prefix is a map, then we store the sub-keys in that map.
        if (!(prefixValue instanceof Map)) {
            return;
        }

        Map nestedMap = (Map)prefixValue;
        if (nestedIndex < nestedKey.length() - 1) {
            String remainderOfKey = nestedKey.substring(nestedIndex + 1, nestedKey.length());
            // GRAILS-2486 Cascade the '_' prefix in order to bind checkboxes properly
            if (prefixedByUnderscore) {
                remainderOfKey = '_' + remainderOfKey;
            }

            nestedMap.put(remainderOfKey, requestMap.get(key));
            if (!(nestedMap instanceof PathKeyMap) && remainderOfKey.indexOf('.') >-1) {
                processNestedKeys(requestMap, remainderOfKey, remainderOfKey, nestedMap);
            }
        }
    }
}
