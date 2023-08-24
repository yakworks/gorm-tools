/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.validation


import spock.lang.Specification
import yakworks.i18n.icu.GrailsICUMessageSource
import yakworks.testing.gorm.model.ValidationEntity
import yakworks.testing.gorm.unit.GormHibernateTest

class DomainValidationSpec extends Specification implements GormHibernateTest  {

    static List entityClasses = [ValidationEntity]

    Closure doWithGormBeans() { { ->
        messageSource(GrailsICUMessageSource){
            searchClasspath = true
            messageBundleLocations = "classpath*:*messages*.properties"
        }
    }}

    void "check validation success"() {
        when:
        def entity = new ValidationEntity(
            xRequired: 'Foo',
            xMinSize: '123',
            xMaxSize: '123',
            xMin: 3,
            xMax: 3,
            xMatches: 'HI',
            xEmail: 'foo@foo.bar',
            xNotBlank: 'x',
            xRange: 2,
            xInList: 'a'
        )

        def isValid = entity.validate()
        def errors = entity.errors

        then:
        isValid
        errors.errorCount == 0
    }

    def makeInvalidEntity(){
        return new ValidationEntity(
            xMinSize:'12',
            xMaxSize:'1234',
            xMin:2,
            xMax:4,
            // xScale2: 1.001,
            xMatches: 'Hello',
            xEmail: 'foo',
            // xNotBlank: '',
            xRange: 4,
            xInList: 'c'
        )
    }

    void "check validations problems"(){
        when:
        def entity = makeInvalidEntity()
        //ControllersDomainBindingApi scrambles the constructor to use binder so manually set it
        entity.xNotBlank = ''

        def isValid = entity.validate()
        def errors = entity.errors
        def codesCount = 2

        then:
        !isValid

        errors['xRequired'].code == ValidationCode.NotNull.name()
        errors['xRequired'].codes.size() == codesCount

        errors['xMinSize'].code == ValidationCode.MinLength.name()
        errors['xMinSize'].codes.size() == codesCount

        errors['xMaxSize'].code == ValidationCode.MaxLength.name()
        errors['xMaxSize'].codes.size() == codesCount

        errors['xMax'].code == ValidationCode.Max.name()
        errors['xMax'].codes.size() == codesCount

        errors['xMin'].code == ValidationCode.Min.name()
        errors['xMin'].codes.size() == codesCount

        // errors['xScale2'].code == ValidationCode.Scale.name()
        errors['xMatches'].code == ValidationCode.Pattern.name()
        errors['xMatches'].codes.size() == codesCount

        errors['xEmail'].code == ValidationCode.Email.name()
        errors['xEmail'].codes.size() == codesCount

        errors['xNotBlank'].code == ValidationCode.NotBlank.name()
        errors['xNotBlank'].codes.size() == codesCount

        // errors['xRange'].code == ValidationCode.Range.name()
        // errors['xRange'].codes.size() == codesCount

        errors['xInList'].code == ValidationCode.InList.name()
        errors['xInList'].codes.size() == codesCount
        errors.errorCount == 9
    }

    void "check validations messages"(){
        when:
        def entity = makeInvalidEntity()
        //ControllersDomainBindingApi scrambles the constructor to use binder so manually set it
        entity.xNotBlank = ''

        def isValid = entity.validate()
        def errors = entity.errors
        def codesCount = 2
        // JakartaValidationCodeAdaptor

        then:
        errors.errorCount == 9
        messageSource.getMessage(errors['xRequired']) == 'must not be null'
        messageSource.getMessage(errors['xMax']) == 'must be less than or equal to 3'
        messageSource.getMessage(errors['xMin']) == 'must be greater than or equal to 3'
        messageSource.getMessage(errors['xMatches']) == 'must match "HI"'
        messageSource.getMessage(errors['xEmail']) == 'must be a well-formed email address'
        messageSource.getMessage(errors['xNotBlank']) == 'must not be blank'
        //messageSource.getMessage(errors['xRange']) == 'must be between 1 and 3'

        messageSource.getMessage(errors['xMinSize']) == 'length must be greater than or equal to 3'
        messageSource.getMessage(errors['xMaxSize']) == 'length must be less than or equal to 3'
        messageSource.getMessage(errors['xInList']) == 'must be in list [a, b]'

    }
}
