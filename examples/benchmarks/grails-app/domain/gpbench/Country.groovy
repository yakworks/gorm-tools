package gpbench

class Country {
    String name
    String capital
    String fips104
    String iso2
    String iso3

    static mapping = {
        id generator: "assigned"
//        cache true
    }

    static constraints = {
        name unique: true
        capital nullable: true
    }

    String toString() { name }

}
