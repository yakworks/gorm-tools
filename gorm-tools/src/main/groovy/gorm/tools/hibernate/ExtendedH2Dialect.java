/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package gorm.tools.hibernate;

import org.hibernate.dialect.H2Dialect;

import java.sql.Types;

/**
 * Extended for Json types
 *
 * @author Thomas Mueller
 */
public class ExtendedH2Dialect extends H2Dialect {

    /**
     * Constructs a H2Dialect
     */
    public ExtendedH2Dialect() {
        super();


        registerColumnType(Types.OTHER, "json");

    }

}
