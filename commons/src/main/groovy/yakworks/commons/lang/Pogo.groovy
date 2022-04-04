/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.lang

import groovy.transform.CompileStatic

import org.codehaus.groovy.runtime.InvokerHelper

import yakworks.commons.map.Maps

/**
 * helpers for Plain Old Groovy Objects and Beans
 *
 * @author Joshua Burnett (@basejump)
 */
@CompileStatic
class Pogo {

    /**
     * shorter and more semanticly correct alias to getProperty
     */
    static Object value(Object source, String property) {
        PropertyTools.getProperty(source, property)
    }


    /**
     * Merge the a map, nested or not, onto the pogo. Uses the InvokerHelper.setProperties(values)
     */
    //FIXME needs test for merge
    static void merge( Map args = [:], Object pogo, Map values){
        boolean ignoreNulls = args.containsKey('ignoreNulls') ? args['ignoreNulls'] : true
        if(ignoreNulls){
            values = Maps.prune(values)
        }
        InvokerHelper.setProperties(pogo, values)
    }

    //standard deep copy implementation
    //take from here https://stackoverflow.com/questions/13155127/deep-copy-map-in-groovy
    //also see @groovy.transform.AutoClone
    def deepcopy(Object orig) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream()
        ObjectOutputStream oos = new ObjectOutputStream(bos)
        oos.writeObject(orig)
        oos.flush()
        ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray())
        ObjectInputStream ois = new ObjectInputStream(bin)
        return ois.readObject()
    }

}
