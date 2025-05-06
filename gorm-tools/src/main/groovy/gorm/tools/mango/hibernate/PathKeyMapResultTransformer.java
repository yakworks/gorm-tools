/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package gorm.tools.mango.hibernate;

import yakworks.commons.map.LazyPathKeyMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.transform.AliasedTupleSubsetResultTransformer;
import org.hibernate.transform.ResultTransformer;

/**
 * {@link ResultTransformer} implementation which builds a map for each "row",
 * removes any aggregate suffixes for "_sum", "
 *
 * @author Joshua Burnett
 */
public class PathKeyMapResultTransformer implements ResultTransformer {

    // removes everything after the last _ on these. so foo_sum will just end up as foo.
    List<String> aliasesToTrimSuffix = new ArrayList<String>();

    //public static final PathKeyMapResultTransformer INSTANCE = new PathKeyMapResultTransformer();

    public PathKeyMapResultTransformer() {
    }

    public PathKeyMapResultTransformer(List<String> aliasesToTrimSuffix) {
        this.aliasesToTrimSuffix = aliasesToTrimSuffix;
    }

    /**
     * removes everything after the last _ thats in the projectionList
     * @param tuple The result elements
     * @param aliases The result aliases ("parallel" array to tuple)
     * @return the map
     */
    @Override
    public Object transformTuple(Object[] tuple, String[] aliases) {
        Map result = new HashMap<String, Object>(tuple.length);
        for (int i = 0; i < tuple.length; i++) {
            String alias = aliases[i];
            if (alias != null) {
                result.put(getKeyName(alias), tuple[i]);
            } else {
                //for null just label it wil the index
                result.put("null" + (i + 1), tuple[i]);
            }
        }
        var pathMap = LazyPathKeyMap.of(result);
        return pathMap;
    }

    @Override
    public List transformList(List collection) {
        return collection;
    }

    String getKeyName(String alias) {
        int endIndex = alias.lastIndexOf("_");
        //if its in the list, for example an aggregate like amount_sum, amount_avg, _etc then trim off the _sum, _avg, _etc part
        if (aliasesToTrimSuffix.contains(alias) && endIndex != -1) {
            alias = alias.substring(0, endIndex); // not forgot to put check if(endIndex != -1)
        }
        //replace the _ with dots XXX what if user puts their own alias in?
        alias = alias.replace('_', '.');
        return alias;
    }

}
