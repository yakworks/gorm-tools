package skydive

import gorm.tools.repository.RepoEntity

@RepoEntity
class Jumper {

    String name
    Long skydives = 0
    //Student student

    static constraints = {
        name nullable:false
    }
}
