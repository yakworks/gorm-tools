package gorm.tools.hibernate.criteria


enum Statements {
    INLIST(["in()", "inList()"]){
        public void restrict(delegate, Map params) {
            delegate.inList params.key, params.value
        }
    },
    BETWEEN(["between()"]){
        public void restrict(delegate, Map params) {
            delegate.gte params.key, params.value[0]
            delegate.lte params.key, params.value[1]
        }
    }

    final List<String> statements

    abstract void restrict(delegate, Map params)

    Statements(List<String> state) {
        this.statements = state
    }

    static public Statements findStatement(String statementsValue) {
        for (Statements statement : values()) {
            if (statement.getStatementsValue().contains(statementsValue.toLowerCase()))
                return statement
        }
        return null
    }

    static public List<String> getStatementsList() {
        List<String> statementsList = []
        for (Statements statements : values()) {
            statementsList += statements.statements
        }
        return statementsList
    }


    void restrict(delegate, List paramsList) {
        println paramsList
    }


    public String getStatementsValue() {
        return statements
    }

}