package testing

class Jumper {
    //static transactionalDao = true
    //static dao = 'transactional' //defaults to true
    //static dao = 'transactional'
    //1. we may  want it to be a transactional Dao for the persist methods
    //static daoType = 'transactional'
    //2. we may want to specify a different name than the default ClassNameDao
    //static daoType = 'someOtherDao'
    //3. we may not want to use the dao that exists with ClassNameDao and stick with default
    //static daoType = 'transactional'

    String name
    Long skydives = 0
    //Student student

    static constraints = {
        //student nullable:true
    }
}
