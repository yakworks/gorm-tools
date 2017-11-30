package gorm.tools.databinding

import gorm.tools.beans.DateUtil
import grails.databinding.converters.ValueConverter
import grails.gorm.annotation.Entity
import grails.testing.gorm.DomainUnitTest
import org.grails.databinding.converters.ConversionService
import org.grails.databinding.converters.DateConversionHelper
import spock.lang.Specification

class FastBinderUnitSpec extends Specification implements DomainUnitTest<TestDomain> {

	void "should bind numbers without going through converters"() {
		setup:
		FastBinder binder = new FastBinder()
		ValueConverter longConverter = Mock(ValueConverter)
		binder.conversionHelpers.put(Long, [longConverter])
		binder.conversionService = Mock(ConversionService)

		TestDomain domain = new TestDomain()

		when:
		binder.bind(domain, [age: "100"])

		then:
		0 * longConverter.canConvert(_)
		0 * binder.conversionService.canConvert(_, _)
		domain.age == 100L
	}

	void "should bind date without going through converters"() {
		setup:
		FastBinder binder = new FastBinder()
		DateConversionHelper dateConverter = Mock(DateConversionHelper)
		binder.conversionHelpers.put(Date, [dateConverter])

		TestDomain domain = new TestDomain()

		when:
		binder.bind(domain, [dob: "2017-10-10"])

		then:
		0 * dateConverter.canConvert(_)
		domain.dob == DateUtil.parseJsonDate("2017-10-10")
	}


	void "should fallback to conversion helpers"() {
		setup:
		FastBinder binder = new FastBinder()
		ValueConverter currencyConverter = Mock(ValueConverter)
		binder.conversionHelpers.put(Currency, [currencyConverter])

		TestDomain domain = new TestDomain()

		when:
		binder.bind(domain, [currency: "INR"])

		then:
		1 * currencyConverter.canConvert("INR") >> true
		1 * currencyConverter.convert("INR") >> Currency.getInstance("INR")

		domain.currency == Currency.getInstance("INR")
	}

	void "should fallback to conversion service if no converversion helpers found"() {
		setup:
		FastBinder binder = new FastBinder()
		ConversionService conversionService = Mock(ConversionService)
		binder.conversionService = conversionService
		TestDomain domain = new TestDomain()

		when:
		binder.bind(domain, [currency: "INR"])

		then:
		1 * conversionService.canConvert(_, _) >> true
		1 * conversionService.convert("INR", _) >> Currency.getInstance("INR")

		domain.currency == Currency.getInstance("INR")
	}
}


@Entity
class TestDomain {
	String name
	Long age
	Date dob
	Currency currency
}
