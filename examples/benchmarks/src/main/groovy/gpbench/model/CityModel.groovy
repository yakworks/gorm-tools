package gpbench.model

import gpbench.Country
import gpbench.Region
import groovy.transform.CompileStatic

@CompileStatic
trait CityModel {

	String name
	String shortCode

	BigDecimal latitude
	BigDecimal longitude

	Region region
	Country country
    String state
    String countryName

	Date dateCreated
	Date lastUpdated
	Long dateCreatedUser
	Long lastUpdatedUser

}
