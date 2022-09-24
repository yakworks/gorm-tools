/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package gorm.tools.hibernate.proxy;

import org.hibernate.bytecode.internal.bytebuddy.ByteBuddyState;
import org.hibernate.bytecode.internal.bytebuddy.ProxyFactoryFactoryImpl;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.proxy.ProxyFactory;
import org.hibernate.proxy.pojo.bytebuddy.ByteBuddyProxyHelper;

public class GroovyProxyFactoryFactory extends ProxyFactoryFactoryImpl {

	private final ByteBuddyProxyHelper byteBuddyProxyHelper;

	public GroovyProxyFactoryFactory(ByteBuddyState byteBuddyState, ByteBuddyProxyHelper byteBuddyProxyHelper) {
        super(byteBuddyState, byteBuddyProxyHelper);
		this.byteBuddyProxyHelper = byteBuddyProxyHelper;
	}

	@Override
	public ProxyFactory buildProxyFactory(SessionFactoryImplementor sessionFactory) {
		return new ByteBuddyGroovyProxyFactory( byteBuddyProxyHelper );
	}

}
