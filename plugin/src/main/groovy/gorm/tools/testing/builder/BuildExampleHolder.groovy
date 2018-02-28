/* Copyright 2018. 9ci Inc. Licensed under the Apache License, Version 2.0 */
package gorm.tools.testing.builder

import groovy.transform.CompileStatic

/**
 * Holder for BuildExampleData of test classes that allows to build test values, some sort of cache
 */
@CompileStatic
class BuildExampleHolder {
    private static Map<Class, BuildExampleData> holder = [:]

    /**
     * If holder already contains BuildExampleData for the class gets it from map, create new otherwise
     *
     * @param clazz domain class
     * @return instance of BuildExampleData
     */
    static BuildExampleData get(Class clazz) {
        if (!holder.containsKey(clazz)) {
            holder[clazz] = new BuildExampleData(clazz)
        }
        holder[clazz]
    }

    /**
     * Clears holder
     */
    static void clear() {
        holder.clear()
    }
}
