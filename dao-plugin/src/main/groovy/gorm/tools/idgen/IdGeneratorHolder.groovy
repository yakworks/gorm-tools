package gorm.tools.idgen

import groovy.transform.CompileStatic

@CompileStatic
public class IdGeneratorHolder {
	public static IdGenerator idGenerator

	public IdGenerator getIdGenerator() {
		return idGenerator
	}

	public void setIdGenerator(IdGenerator idGenerator) {
		if(IdGeneratorHolder.idGenerator == null) {
			IdGeneratorHolder.idGenerator = idGenerator
		} // else {
		// 			throw new IllegalArgumentException("There is already a generator set!")
		// 		}
	}
}
