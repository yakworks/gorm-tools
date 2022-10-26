/*
* Copyright 2013-2016 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.token

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

import org.springframework.security.core.AuthenticationException

/**
 * Thrown if the desired token is not found by the {@link TokenStorageService}
 */
@InheritConstructors
@CompileStatic
class TokenNotFoundException extends AuthenticationException {}
