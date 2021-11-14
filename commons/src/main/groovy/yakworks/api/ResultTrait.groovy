/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.api

import groovy.transform.CompileStatic

import yakworks.i18n.MsgKey
import yakworks.i18n.MsgKeyTrait

// import org.springframework.http.HttpStatus

/**
 * Trait impl for the Result
 *
 * example:
 *
 * <pre> {@code class ResultImp implements ResultTrait<ResultImp, Map> ...
    Then for builders you can do
    def foo = ResultImp.of(200).code('some.key').params([name: 'foo'])
    def foo = ResultImp.ofMessage('some.key', [name: 'foo']) }
 * </pre>
 *
 * @param <E> Implementing this trait, used for builder method return types
 * @param <D> Data, will normally be a Map or a List but can be anything
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
trait ResultTrait<E,D> implements Result<D>{
    Boolean ok = true

    ApiStatus status = HttpStatus.OK
    MsgKey msgKey
    String title

    D data
    Object target

    // // Builders
    E title(String v) { title = v;  return (E)this; }
    E data(D v)       { data = v;   return (E)this; }
    E target(Object v){ target = v; return (E)this; }

    E msgKey(MsgKey v){ msgKey = v; return (E)this; }
    E msgKey(String v, Map args) {
        msgKey = MsgKey.of(v, args);
        return (E)this;
    }

    E status(ApiStatus v) { status = v; return (E)this; }
    E status(Integer v){ status = HttpStatus.valueOf(v); return (E)this; }

}
