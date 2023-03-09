package gorm.tools.mango.hibernate;

import org.hibernate.transform.ResultTransformer;
import java.lang.FunctionalInterface;
import java.util.List;

@FunctionalInterface
public interface ListResultTransformer extends ResultTransformer {

    /**
     * Default implementation returning the tuples list as-is.
     *
     * @param tuples tuples list
     * @return tuples list
     */
    @Override
    default List transformList(List tuples) {
        return tuples;
    }
}
