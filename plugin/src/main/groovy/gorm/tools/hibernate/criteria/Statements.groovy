package gorm.tools.hibernate.criteria

import groovy.transform.CompileDynamic

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
            [statements: ["\$gte", "\$ge"], restriction:
                    { delegate, Map params ->
                        delegate.ge params.keySet()[0], params.values()[0][0]
                    }
            ],
            [statements: ["\$lt"], restriction:
                    { delegate, Map params ->
                        delegate.lt params.keySet()[0], params.values()[0][0]
                    }
            ],
            [statements: ["\$lte", "\$le"], restriction:
                    { delegate, Map params ->
                        delegate.le params.keySet()[0], params.values()[0][0]
                    }
            ],
            [statements: ["\$gtf"], type:StatementsType.PROPERTY, restriction:
                    { delegate, Map params ->
                        delegate.gtProperty params.keySet()[0], params.values()[0][0]
                    }
            ],
            [statements: ["\$gtef"], type:StatementsType.PROPERTY, restriction:
                    { delegate, Map params ->
                        delegate.geProperty params.keySet()[0], params.values()[0][0]
                    }
            ],
            [statements: ["\$ltf"], type:StatementsType.PROPERTY, restriction:
                    { delegate, Map params ->
                        delegate.ltProperty params.keySet()[0], params.values()[0][0]
                    }
            ],
            [statements: ["\$ltef"], type:StatementsType.PROPERTY, restriction:
                    { delegate, Map params ->
                        println "\$ltef      k: ${params.keySet()[0]}   val: ${params.values()[0][0]}"
                        delegate.leProperty(params.keySet()[0], params.values()[0][0])
                    }
            ],
            [statements: ["\$ne"], restriction:
                    { delegate, Map params ->
                        delegate.not {
                            delegate.eq params.keySet()[0], params.values()[0][0]
                        }
                    }
            ],
            [statements: ["\$isNull", "null"], type:StatementsType.UNARY, restriction:
                    { delegate, Map params ->
                        delegate.isNull params.keySet()[0]
                    }
            ],
            [statements: ["\$or", "\$and"], type: StatementsType.OPERATORS]
    ]
    /**
     * Get list of all alloweded statements
     *
     * @return list with all statements
     */
    @CompileDynamic
    static List<String> listAllowedStatements() {
        statements*.statements.flatten()
    }

    /**
     * Get list of allowed statements with specific type
     *
     * @return list with statements with specific type
     */
    @CompileDynamic
    static List<String> listAllowedStatements(StatementsType type) {
        statements.findAll{it.type == type}*.statements.flatten()
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
enum StatementsType{
    PROPERTY, UNARY, OPERATORS
}
