/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package yakworks.gorm.hibernate.dialect;

import org.hibernate.dialect.PostgreSQL10Dialect;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.type.StandardBasicTypes;

/**
 * adds custom functions such as flike
 */
public class ExtendedPostgreSQLDialect extends PostgreSQL10Dialect {

    public ExtendedPostgreSQLDialect() {
        super();
        registerFunction( "flike", new VarArgsSQLFunction(StandardBasicTypes.BOOLEAN, "(", " ilike ", ")"));
    }

}
