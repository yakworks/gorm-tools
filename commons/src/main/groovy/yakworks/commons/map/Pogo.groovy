/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.map

import groovy.transform.CompileStatic

import org.codehaus.groovy.runtime.InvokerHelper

/**
 * helpers for Plain Old Groovy Objects and Beans
 *
 * @author Joshua Burnett (@basejump)
 */
@CompileStatic
class Pogo {

    /**
     * Merge the a map, nested or not, onto the pogo. Uses the InvokerHelper.setProperties(values)
     */
    static void merge( Map args = [:], Object pogo, Map values){
        boolean ignoreNulls = args.containsKey('ignoreNulls') ? args['ignoreNulls'] : true
        if(ignoreNulls){
            values = Maps.prune(values)
        }
        InvokerHelper.setProperties(pogo, values)
    }

//    public static void setProperties(Object object, Map map) {
//        MetaClass mc = getMetaClass(object);
//        Iterator var3 = map.entrySet().iterator();
//
//        while(var3.hasNext()) {
//            Object o = var3.next();
//            Map.Entry entry = (Map.Entry)o;
//            String key = entry.getKey().toString();
//            Object value = entry.getValue();
//            setPropertySafe(object, mc, key, value);
//        }
//
//    }
//
//    private static void setPropertySafe(Object object, MetaClass mc, String key, Object value) {
//        try {
//            mc.setProperty(object, key, value);
//        } catch (MissingPropertyException var6) {
//            ;
//        } catch (InvokerInvocationException var7) {
//            Throwable cause = var7.getCause();
//            if (cause == null || !(cause instanceof IllegalArgumentException)) {
//                throw var7;
//            }
//        }
//
//    }

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
