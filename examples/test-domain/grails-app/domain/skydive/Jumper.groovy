package skydive

import gorm.tools.repository.model.RepoEntity

class Jumper implements RepoEntity<Jumper>{

    String name
    Long skydives = 0
    //Student student

    static constraints = {
        name nullable:false
    }
}
