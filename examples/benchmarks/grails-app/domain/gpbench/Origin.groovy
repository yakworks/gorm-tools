package gpbench

class Origin {

    Country country
    Region state
    City city

    static constraints = {
        country 	nullable:false
        state 		nullable:true
        city		nullable:true
    }
}
