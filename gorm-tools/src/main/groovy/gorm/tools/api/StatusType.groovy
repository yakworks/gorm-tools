/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.api

import groovy.transform.CompileStatic

/**
 * Base interface for statuses.
 */
@CompileStatic
interface StatusType {

    /**
     * Get the associated status code.
     *
     * @return the status code.
     */
    int getStatusCode();

    /**
     * Get the reason phrase.
     *
     * @return the reason phrase.
     */
    String getReasonPhrase();

}
