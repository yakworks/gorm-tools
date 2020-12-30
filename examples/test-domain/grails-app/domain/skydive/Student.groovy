package skydive

import gorm.tools.repository.RepoEntity

@RepoEntity
class Student {
    Jumper jumper
    String name

    static constraints = {
    }
}
