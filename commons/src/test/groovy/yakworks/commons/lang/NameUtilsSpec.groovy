/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.lang


import spock.lang.Specification
import spock.lang.Unroll
import yakworks.commons.lang.PropertyTools
import yakworks.commons.testing.pogos.Gadget

class NameUtilsSpec extends Specification{

    @Unroll
    void "property name #name"() {
        expect:
        NameUtils.getPropertyName(name) == result

        where:
        name                   | result
        "bar"                  | 'bar'
        "FooBar"               | "fooBar"
        "URLoo"                | "URLoo"
        ".alpha.baker.charlie" | "charlie"
    }

    @Unroll
    void "naturla name #name"() {
        expect:
        NameUtils.getNaturalName(name) == result

        where:
        name                           | result
        "firstName"                    | "First Name"
        "aName"                        | "A Name"
        "URL"                          | "URL"
        "localURL"                     | "Local URL"
        "aURLlocal"                    | "A URL local"
        "MyDomainClass"                | "My Domain Class"
        "com.myco.myapp.MyDomainClass" | "My Domain Class"
    }

    void "test simple name"() {
        expect:
        NameUtils.getSimpleName(value) == result

        where:
        value               | result
        "com.fooBar.FooBar" | "FooBar"
        "FooBar"            | "FooBar"
        "com.bar.\$FooBar"  | "\$FooBar"
    }

    @Unroll
    void "test camel case value #value"() {
        expect:
        NameUtils.camelCase(value) == result

        where:
        value                             | result
        'micronaut.config-client.enabled' | 'micronaut.configClient.enabled'
        'foo-bar'                         | 'fooBar'
        'SOME_PROP'|'someProp'
    }


    @Unroll
    void "test hyphenate #value"() {
        expect:
        NameUtils.hyphenate(value) == result

        where:
        value                                       | result
        'gr8crm-notification-service'.toUpperCase() | 'gr8crm-notification-service'
        'gr8-notification-service'.toUpperCase()    | 'gr8-notification-service'
        '8gr8-notification-service'.toUpperCase()   | '8gr8-notification-service'
        '8gr8-notification-s3rv1c3'.toUpperCase()   | '8gr8-notification-s3rv1c3'
        'gr8-7notification-service'.toUpperCase()   | 'gr8-7notification-service'
        'ec55Metadata'                              | 'ec55-metadata'
        'micronaut.config-client.enabled'           | 'micronaut.config-client.enabled'
        "com.fooBar.FooBar"                         | "com.foo-bar.foo-bar"
        "FooBar"                                    | "foo-bar"
        "com.bar.FooBar"                            | "com.bar.foo-bar"
        "Foo"                                       | 'foo'
        "FooBBar"                                   | 'foo-bbar'
        "FOO_BAR"                                   | 'foo-bar'
        "fooBBar"                                   | 'foo-bbar'
        'gr8crm-notification-service'               | 'gr8crm-notification-service'
        'aws.disableEc2Metadata'                    | 'aws.disable-ec2-metadata'
        'aws.disableEcMetadata'                     | 'aws.disable-ec-metadata'
        'aws.disableEc2instanceMetadata'            | 'aws.disable-ec2instance-metadata'
    }


    void "test decapitalize"() {
        expect:
        NameUtils.decapitalize(name) == result

        where:
        name    | result
        "Title" | "title"
        "T"     | "t"
        "TiTLE" | "tiTLE"
        "aBCD"  | "aBCD"
        "ABCD"  | "ABCD"
        "aBC"   | "aBC"
        "ABC"   | "ABC"
        "AB"    | "AB"
        "ABc"   | "aBc"
    }

    void "test decapitalize returns same ref"() {
        expect:
        NameUtils.decapitalize(name).is(name)

        where:
        name      | _
        ""        | _
        "a"       | _
        "aa"      | _
        "aB"      | _
        "AA"      | _
        "ABCD"    | _
        "a a"     | _
        "abcd ef" | _
    }

}
