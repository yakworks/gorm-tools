package skydive

import gorm.tools.repository.model.RepoEntity

class Student implements RepoEntity<Student>{
    Jumper jumper
    String name

    static constraints = {
    }
}
