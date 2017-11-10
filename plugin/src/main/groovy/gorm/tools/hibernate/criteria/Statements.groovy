package gorm.tools.hibernate.criteria

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

class Statements {
    /**
     * List of statements that can be used in search
     */ //TODO: maybe should be able to extended from config
    private static List statements = [
            [statements: ["\$in", "\$inList"], restriction:
                    { delegate, Map params ->
                        delegate.in params.keySet()[0], params.values()[0]
                    }
            ],
            [statements: ["\$nin"], restriction:
                    { delegate, Map params ->
                        delegate.not {
                            delegate.in params.keySet()[0], params.values()[0]
                        }
                    }
            ],
            [statements: ["\$between"], restriction:
                    { delegate, Map params ->
                        delegate.gte params.keySet()[0], params.values()[0][0]
                        delegate.lte params.keySet()[0], params.values()[0][1]
                    }
            ],
            [statements: ["\$ilike"], restriction:
                    { delegate, Map params ->
                        delegate.ilike params.keySet()[0], params.values()[0][0]
                    }
            ],
            [statements: ["\$quickSearch"], restriction:
                    { delegate, Map params ->
                        if (params.values()[0][0] instanceof String){
                            delegate.ilike params.keySet()[0], (params.values()[0][0].contains("%")?params.values()[0][0]:(params.values()[0][0]+"%"))
                        } else {
                            delegate.eq params.keySet()[0], params.values()[0][0]
                        }

                    }
            ],
            [statements: ["\$like"], restriction:
                    { delegate, Map params ->
                        delegate.like params.keySet()[0], params.values()[0][0]
                    }
            ],
            [statements: ["\$gt"], restriction:
                    { delegate, Map params ->
                        delegate.gt params.keySet()[0], params.values()[0][0]
                    }
            ],
            [statements: ["\$ge"], restriction:
                    { delegate, Map params ->
                        println "ge ${params.keySet()[0]}, ${params.values()[0][0]}"
                        delegate.ge params.keySet()[0], params.values()[0][0]
                    }
            ],
            [statements: ["\$lt"], restriction:
                    { delegate, Map params ->
                        delegate.lt params.keySet()[0], params.values()[0][0]
                    }
            ],
            [statements: ["\$le"], restriction:
                    { delegate, Map params ->
                        delegate.le params.keySet()[0], params.values()[0][0]
                    }
            ],
            [statements: ["\$ne"], restriction:
                    { delegate, Map params ->
                        delegate.ne params.keySet()[0], params.values()[0][0]
                    }
            ],
            [statements: ["\$isNull"], restriction:
                    { delegate, Map params ->
                        delegate.isNull params.keySet()[0], params.values()[0][0]
                    }
            ]
    ]
    /**
     * Get list of all alloweded statements
     *
     * @return list with all statements
     */
    @CompileDynamic
    static List<String> listAllowedStatements() {
        statements.collect { it.statements}.flatten()
    }
    /**
     * Returns closure that should be executed for statement
     *
     * @param statement statement name
     * @return closure that should be executed for statement
     */
    @CompileDynamic
    static Closure findRestriction(String statement){
        statements.find{it.statements.contains(statement)}.restriction
    }


}