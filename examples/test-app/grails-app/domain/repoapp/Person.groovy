package repoapp

class Person {
    String name
    Integer age

    Address address

    static constraints = {
        address bindable:true
    }
}
