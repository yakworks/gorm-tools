/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package gorm.tools.mango.hibernate;
import java.util.*;
import org.hibernate.transform.AliasedTupleSubsetResultTransformer;
import org.hibernate.transform.ResultTransformer;

/**
 * {@link ResultTransformer} implementation which builds a map for each "row",
 * removes any aggregate suffixes for "_sum", "
 *
 * @author Joshua Burnett
 */
public class AliasProjectionResultTransformer extends AliasedTupleSubsetResultTransformer {

    java.util.List<String> includeAliases = new ArrayList<String>();

	public static final AliasProjectionResultTransformer INSTANCE = new AliasProjectionResultTransformer();

	/**
	 * Disallow instantiation of AliasToEntityMapResultTransformer.
	 */
	public AliasProjectionResultTransformer() {
	}

    /**
     * Disallow instantiation of AliasToEntityMapResultTransformer.
     */
    public AliasProjectionResultTransformer(java.util.List<String> includeAliases ) {
        this.includeAliases = includeAliases;
    }

    /**
     * removes evrything after the last _ thats in the projectionList
     * @param tuple The result elements
     * @param aliases The result aliases ("parallel" array to tuple)
     * @return the map
     */
	@Override
	public Object transformTuple(Object[] tuple, String[] aliases) {
		Map result = new HashMap(tuple.length);
		for ( int i=0; i<tuple.length; i++ ) {
			String alias = aliases[i];
			if ( alias != null ) {
				result.put( getKeyName(alias), tuple[i] );
			} else {
                //for null just label it wil the index
                result.put( "null"+(i+1), tuple[i] );
            }
		}
		return result;
	}

    String getKeyName(String alias){
        int endIndex = alias.lastIndexOf("_");
        if (includeAliases.contains(alias) && endIndex != -1) {
            alias = alias.substring(0, endIndex); // not forgot to put check if(endIndex != -1)
        }
        return alias;
    }

	@Override
	public boolean isTransformedValueATupleElement(String[] aliases, int tupleLength) {
		return false;
	}

	/**
	 * Serialization hook for ensuring singleton uniqueing.
	 *
	 * @return The singleton instance : {@link #INSTANCE}
	 */
	private Object readResolve() {
		return INSTANCE;
	}
}
