package net.thevis.groovyhadoop

import spock.lang.Specification;

/**
 * @author Thomas Thevis
 *
 */
class MainSpecification extends Specification {

	
	def "valid input values for 'parseBytes()'"() {
		given: 
			def fixture = new Main() 
		expect:
			result == fixture.parseBytes(input)
		where:	
			input | result
			"1234"  | 1234
			"12M"	| 12582912
			"12m"	| 12582912
			"12G"	| 12884901888
			"12g"	| 12884901888
	}
	
	def "invalid values for 'parseBytes()'"() {
		given:
			def fixture = new Main()
		when:
			fixture.parseBytes(invalidInput)
		then:
			def ex = thrown(RuntimeException)
			ex.message == message
		where:
			invalidInput | message	
			""           | "invalid input value: "
			"12MB"       | "invalid input value: 12MB"
			"12 M"       | "invalid input value: 12 M"
			"12 "        | "invalid input value: 12 "
			" 12"        | "invalid input value:  12"
	}
	
}
