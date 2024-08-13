package yakworks.rally.mail

import yakworks.api.Result
import yakworks.api.problem.ThrowableProblem

import javax.mail.internet.InternetAddress

import spock.lang.Specification

import static yakworks.rally.mail.EmailUtils.validateEmail

class EmailUtilsSpec extends Specification {

    void "validate emails fail"() {
        expect:
        assertInvalid('jimjoe.com', null, "Invalid email address [jimjoe.com], Missing final '@domain'")
    }

    void "validate bad email in list"() {
        expect:
        assertInvalid('Account Services <rndc@greenbill.io>,jimjoe.com', null, "Missing final '@domain'")
    }

    void "validate addresses"() {
        expect: "valid"
        validateEmail("test@test.com")
        validateEmail('"John Doe" <test@test.com>')
        validateEmail('"John, Doe" <test@test.com>')
        validateEmail('one@one.com, two@two.om')
        validateEmail('"One one" <one@one.com>, "Two, two" <two@two.com>')
        validateEmail('Account Services <rndc@greenbill.io>, "Blow, Joe" <josh2@yak.com>,joe@email.com')

        and: "invalid"
        assertInvalid(null, "error.data.empty")
        assertInvalid("", "error.data.empty")
        assertInvalid(" ", "error.data.empty")
        assertInvalid('jimjoe.com')
        assertInvalid("test@test.com.")
        assertInvalid('John Doe')
        assertInvalid('test@test.com, jimjoe.com') //single bed item

    }

    void "InternetAddress playground"() {
        when:
        def iaddy = new InternetAddress('"Blow, Joe" <josh2@9ci.com>')
        InternetAddress[] listAddy = InternetAddress.parse('Account Services <rndc@greenbill.io>, "Blow, Joe" <josh2@9ci.com>,jim@joe.com');

        then:
        iaddy.address == "josh2@9ci.com"
        iaddy.validate()
        iaddy.personal == "Blow, Joe"
        iaddy.toString() == '"Blow, Joe" <josh2@9ci.com>'

        listAddy.size() == 3
        listAddy[0].toString() == "Account Services <rndc@greenbill.io>"
        listAddy[0].address == "rndc@greenbill.io"
        listAddy[0].personal == "Account Services"
        listAddy[0].validate()

        listAddy[1].toString() == "\"Blow, Joe\" <josh2@9ci.com>"
        listAddy[1].address == "josh2@9ci.com"
        listAddy[1].personal == "Blow, Joe"
        listAddy[1].validate()

        listAddy[2].validate()
        listAddy[2].toString() == "jim@joe.com"
        listAddy[2].address == "jim@joe.com"
        listAddy[2].personal == null
    }

    void "InternetAddress simple list"() {
        when:
        InternetAddress[] listAddy = InternetAddress.parse('rndc@greenbill.io, josh2@9ci.com, jim@joe.com')

        then:
        listAddy.size() === 3
        listAddy.each {
            it.validate()
            assert it.address
        }
    }

    void "InternetAddress single item"() {
        when:
        InternetAddress[] listAddy = InternetAddress.parse('jim@joe.com')

        then:
        listAddy.size() === 1
        listAddy[0].address == 'jim@joe.com'
        listAddy.each {
            it.validate()
            assert it.address
        }
    }

    Result assertInvalid(String mail, String code = null, String detail = null) {
        try {
            validateEmail(mail)
        } catch(ThrowableProblem p) {
            if(code) assert p.code == code
            if(detail) assert p.detail.contains(detail)
            return p
        }
    }
}
