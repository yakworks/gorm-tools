package gpbench.model.fat

import java.time.LocalDate
import java.time.LocalDateTime

import org.grails.datastore.gorm.GormEnhancer

import gorm.tools.repository.model.RepoEntity
import gpbench.model.Country
import gpbench.model.Region
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.lang.IsoDateUtil

/**
 * Without traits and nullable: false on associations
 */
@Entity
@GrailsCompileStatic
class CityFatNoTraits implements RepoEntity<CityFatNoTraits> {
    // static belongsTo = [ region: Region, country: Country,
    //                      region2: Region, country2: Country,
    //                      region3: Region, country3: Country ]

    Region region
    Country country
    Region region2
    Country country2
    Region region3
    Country country3

    String name
    String shortCode
    BigDecimal latitude
    BigDecimal longitude
    String state
    String countryName

    Date dateCreated
    Date lastUpdated

    //these don't do anything and are just here to equalize the number of fields
    Long dateCreatedUser
    Long lastUpdatedUser

    String name2
    String shortCode2
    String state2
    String countryName2

    BigDecimal latitude2
    BigDecimal longitude2

    String name3
    String shortCode3
    String state3
    String countryName3

    BigDecimal latitude3
    BigDecimal longitude3

    Date date1
    LocalDate date2
    LocalDateTime date3
    LocalDate date4

    static constraints = {
        name nullable: false
        shortCode nullable: false
        latitude nullable: false, scale: 4, max: 90.00
        longitude nullable: false, scale: 4, max: 380.00
        region nullable: false
        country nullable: false
        state nullable: true
        countryName nullable: true

        name2 nullable: false
        shortCode2 nullable: false
        latitude2 nullable: false, scale: 4, max: 90.00
        longitude2 nullable: false, scale: 4, max: 380.00
        // region2 nullable: false
        // country2 nullable: false
        state2 nullable: true
        countryName2 nullable: true

        name3 nullable: false
        shortCode3 nullable: false
        latitude3 nullable: false, scale: 4, max: 90.00
        longitude3 nullable: false, scale: 4, max: 380.00
        // region3 nullable: false
        // country3 nullable: false
        state3 nullable: true
        countryName3 nullable: true

        dateCreated nullable: true, display: false, editable: false, bindable: false
        lastUpdated nullable: true, display: false, editable: false, bindable: false
        dateCreatedUser nullable: true, display: false, editable: false, bindable: false
        lastUpdatedUser nullable: true, display: false, editable: false, bindable: false

        date1 nullable: true
        date2 nullable: true
        date3 nullable: true
        date4 nullable: true
    }

    // static mapping = {
    //     id generator: "native"
    // }

    String toString() { name }

    void setProps(Map row) {
        this.name = row['name']
        this.shortCode = row['shortCode']
        this.state = row['state']
        this.countryName = row['countryName']
        this.latitude = row['latitude'] as BigDecimal
        this.longitude = row['longitude'] as BigDecimal

        this.name2 = row['name2']
        this.shortCode2 = row['shortCode2']
        this.state2 = row['state2']
        this.countryName2 = row['countryName2']
        this.latitude2 = row['latitude2'] as BigDecimal
        this.longitude2 = row['longitude2'] as BigDecimal

        this.name3 = row['name3']
        this.shortCode3 = row['shortCode3']
        this.state3 = row['state3']
        this.countryName3 = row['countryName3']
        this.latitude3 = row['latitude3'] as BigDecimal
        this.longitude3 = row['longitude3'] as BigDecimal
        //this.properties = row
        date1 = IsoDateUtil.parse(row['date1'] as String)
        date2 = IsoDateUtil.parseLocalDate(row['date2'] as String) //DateUtil.parse(row['date2'] as String)
        date3 = IsoDateUtil.parseLocalDateTime(row['date3'] as String)
        date4 = IsoDateUtil.parseLocalDate(row['date4'] as String)

        setAssociation("region", Region, row)
        setAssociation("region2", Region, row)
        setAssociation("region3", Region, row)
        setAssociation("country", Country, row)
        setAssociation("country2", Country, row)
        setAssociation("country3", Country, row)
    }

    void setAssociation(String key, Class assocClass, Map row) {
        if (row[key] != null) {
            Long id = row[key]['id'] as Long
            this[key] = GormEnhancer.findStaticApi(assocClass).load(id)
        }
    }

}
