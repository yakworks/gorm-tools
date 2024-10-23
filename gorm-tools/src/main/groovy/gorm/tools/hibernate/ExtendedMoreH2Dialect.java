/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package gorm.tools.hibernate;

import java.sql.Types;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.type.StandardBasicTypes;

/**
 * adds json and custom fn_ilike function
 */
public class ExtendedMoreH2Dialect extends H2Dialect {

    /**
     * Constructs a H2Dialect
     */
    public ExtendedMoreH2Dialect() {
        super();
        registerColumnType(Types.OTHER, "json");
        registerColumnType( Types.VARBINARY, "BLOB" );
        //registerFunction( "fn_ilike", new SQLFunctionTemplate(StandardBasicTypes.BOOLEAN, "?1 ilike ?2", false));
        registerFunction( "fn_ilike", new VarArgsSQLFunction(StandardBasicTypes.BOOLEAN, "(", " ilike ", ")"));
    }

}
