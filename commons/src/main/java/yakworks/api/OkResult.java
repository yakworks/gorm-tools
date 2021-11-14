/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.api;

import java.io.Serializable;

/**
 * Simple OkResult with a Map as the data object
 *
 * @author Joshua Burnett (@basejump)
 */
public class OkResult extends AbstractResult<OkResult> implements Serializable {

    static OkResult get() {
        return new OkResult();
    }

}
