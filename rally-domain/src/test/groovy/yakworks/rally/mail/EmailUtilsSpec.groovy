package yakworks.rally.mail

import javax.mail.internet.InternetAddress

import spock.lang.Specification
import yakworks.api.problem.ThrowableProblem

class EmailUtilsSpec extends Specification {

    void "validate emails"() {
        expect:
        EmailUtils.validateEmail('jim@joe.com')
        EmailUtils.validateEmail('Account Services <rndc@greenbill.io>, "Blow, Joe" <josh2@yak.com>,joe@email.com')
    }

    void "validate emails fail"() {
        when:
        EmailUtils.validateEmail('jimjoe.com')
        then:
        ThrowableProblem ex = thrown()
        ex.code == "validation.problem"
        ex.detail == "Missing final '@domain'"
    }

    void "validate bad email in list"() {
        when:
        EmailUtils.validateEmail('Account Services <rndc@greenbill.io>,jimjoe.com')
        then:
        ThrowableProblem ex = thrown()
        ex.code == "validation.problem"
        ex.detail == "Missing final '@domain'"
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
}
