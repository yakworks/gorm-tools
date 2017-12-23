package gorm.tools.idgen

import groovy.transform.CompileStatic

@SuppressWarnings(['NonFinalPublicField'])
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
