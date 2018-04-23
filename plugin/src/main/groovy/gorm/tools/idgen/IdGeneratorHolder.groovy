/* Copyright 2018. 9ci Inc. Licensed under the Apache License, Version 2.0 */
package gorm.tools.idgen

import groovy.transform.CompileStatic

/**
 * @Deprecated do not use anymore
 */
@Deprecated
@CompileStatic
class IdGeneratorHolder {
    public static IdGenerator idGenerator

    IdGenerator getIdGenerator() {
        return idGenerator
    }

    void setIdGenerator(IdGenerator idGenerator) {
        if (IdGeneratorHolder.idGenerator == null) {
            IdGeneratorHolder.idGenerator = idGenerator
        }
    }
}
