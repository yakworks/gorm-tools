ruleset {
    ruleset('rulesets/basic.xml'){
        'EmptyClass' doNotApplyToFilesMatching: '.*Spec.groovy'
    }

    ruleset('rulesets/braces.xml'){
        exclude 'IfStatementBraces'
    }

    ruleset('rulesets/concurrency.xml')

    ruleset('rulesets/convention.xml'){
        ['CouldBeElvis', 'NoDef', 'ParameterReassignment', 'MethodParameterTypeRequired',
         'MethodReturnTypeRequired', 'CouldBeSwitchStatement', 'InvertedCondition','TrailingComma',
         'VariableTypeRequired'
        ].each{
            exclude it
        }
    }
    // convention
    NoTabCharacter

    ruleset('rulesets/design.xml'){
        ['BuilderMethodWithSideEffects', 'Instanceof',
         'NestedForLoop', 'PrivateFieldCouldBeFinal', 'SimpleDateFormatMissingLocale'
        ].each{
            exclude it
        }
    }

    //ruleset('rulesets/dry.xml')

    //ruleset('rulesets/enhanced.xml')//FIXME try adding in the src to classpath so theese work

    ruleset('rulesets/exceptions.xml')

    // rulesets/formatting.xml
    BlankLineBeforePackage
    BracesForClass
    BracesForForLoop
    BracesForIfElse
    BracesForMethod
    BracesForTryCatchFinally
    //ClassJavadoc
    ClosureStatementOnOpeningLineOfMultipleLineClosure
    ConsecutiveBlankLines
    Indentation
    //FileEndsWithoutNewline
    //'LineLength' doNotApplyToFilesMatching: '*Spec.groovy'
    MissingBlankLineAfterImports
    MissingBlankLineAfterPackage
    //SpaceAfterCatch
    //SpaceAfterFor
    //SpaceAfterIf
    //SpaceAfterSwitch
    //SpaceAfterWhile
    //SpaceAroundClosureArrow
    //SpaceAroundMapEntryColon
    //SpaceAroundOperator
    //SpaceAfterClosingBrace
    SpaceAfterComma
    //SpaceAfterOpeningBrace
    SpaceAfterSemicolon
    //SpaceBeforeClosingBrace
    //SpaceBeforeOpeningBrace
    //TrailingWhitespace

    ruleset('rulesets/generic.xml')

    //ruleset('rulesets/grails.xml')

    ruleset('rulesets/groovyism.xml'){
        exclude 'GetterMethodCouldBeProperty'
    }

    ruleset('rulesets/imports.xml'){
        MisorderedStaticImports(comesBefore:false)
    }

    ruleset('rulesets/jdbc.xml')

    ruleset('rulesets/junit.xml'){
        exclude 'JUnitPublicNonTestMethod'
        exclude 'JUnitPublicProperty'
        exclude 'JUnitPublicNonTestMethod'
    }

    ruleset('rulesets/logging.xml'){
        //exclude 'Println'
    }

    ruleset('rulesets/naming.xml'){
        'MethodName' doNotApplyToFilesMatching: '.*Spec.groovy'
        PropertyName {
            ignorePropertyNames='_*'
        }
        exclude 'ConfusingMethodName'
        exclude 'FactoryMethodName'
    }

    ruleset('rulesets/security.xml'){
        exclude 'JavaIoPackageAccess'
    }

    ruleset('rulesets/serialization.xml'){
        exclude 'SerializableClassMustDefineSerialVersionUID'
    }

    ruleset('rulesets/size.xml'){
        'AbcMetric' doNotApplyToFilesMatching: '.*Spec.groovy'
        'MethodSize' doNotApplyToFilesMatching: '.*Spec.groovy'
        //'ParameterCount' maxParameters: 6
        exclude 'CrapMetric'
        //exclude 'CyclomaticComplexity'
    }

    ruleset('rulesets/unnecessary.xml'){
        'UnnecessaryBooleanExpression' doNotApplyToFilesMatching: '.*Spec.groovy'
        'UnnecessaryObjectReferences' doNotApplyToFilesMatching: '.*Spec.groovy'
        exclude 'ConsecutiveStringConcatenation'
        exclude 'UnnecessaryBooleanExpression'
        exclude 'UnnecessaryGString'
        exclude 'UnnecessaryGetter'
        exclude 'UnnecessaryPublicModifier'
        exclude 'UnnecessaryReturnKeyword'
        exclude 'UnnecessaryDotClass' //FIXME this should be enabled
        exclude 'UnnecessarySetter' //FIXME this should be enabled
        exclude 'UnnecessarySubstring' //FIXME this should be enabled
    }

    ruleset('rulesets/unused.xml'){
        exclude 'UnusedMethodParameter' //FIXME this should be enabled
        exclude 'UnusedVariable' //FIXME this should be enabled
    }

    ruleset('rulesets/codenarc-extra.xml') {
        CompileStatic  {
            doNotApplyToFilesMatching = ".*/src/test/.*|.*GrailsPlugin.groovy|.*Application.groovy"
        }
    }
}
