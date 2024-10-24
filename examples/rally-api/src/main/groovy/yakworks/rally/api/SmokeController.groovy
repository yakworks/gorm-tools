/*
* Copyright 2002-2016 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api


import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.util.logging.Slf4j

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for "/spring".
 */
@RestController
@RequestMapping("/rally/smoke")
@CompileStatic
@Slf4j
class SmokeController {

    @GetMapping()
    String home(@RequestParam String foo, @RequestParam String bar) {
        "hello foo:$foo bar:$bar"
    }

    //optional params without annotations are not required.
    @GetMapping("optionals")
    String noParams(String foo, String bar) {
        "hello foo:$foo bar:$bar"
    }

    @GetMapping("model")
    String modelTest(@ModelAttribute ParmsModel pmodel,
                     ParmsModel2 pmodel2,
                     @RequestParam String foo,
                     String bar
    ) {
        //works with and without the @ModelAttributes and @RequestParamx`
        //binds the model
        assert pmodel.foo
        assert pmodel.foo == pmodel2.foo
        //also binds the foor param
        assert foo == 'buzz'
        "hello " + pmodel
    }

    @ToString(includePackage = false)
    static class ParmsModel {
        String foo
        String bar
    }

    @ToString(includePackage = false)
    static class ParmsModel2 {
        String foo
        String test
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    Map smokeTestPost(@RequestParam String foo,
                      @RequestParam Map<String,String> params,
                      @RequestBody Map payload) {
        assert foo == "buzz"
        params + payload
    }
}
