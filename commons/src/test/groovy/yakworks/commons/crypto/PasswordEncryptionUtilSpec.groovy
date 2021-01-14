package yakworks.commons.crypto

import org.grails.testing.GrailsUnitTest

import spock.lang.Specification
import yakworks.commons.crypto.PasswordEncryptionUtil

class PasswordEncryptionUtilSpec extends Specification implements GrailsUnitTest {

    static ENCRYPTED_PASSWORD = 'v4sFb3vBOrjHplcy3UJJ7Q=='
    static DECRYPTED_PASSWORD = 'greenbill'
    static encryptionPassword = "customPasswordEncryption"

    void testEncryption(){

        when:
        def actualEncrptyedPassword = PasswordEncryptionUtil.encryptBase64(DECRYPTED_PASSWORD , encryptionPassword)

        then:
        ENCRYPTED_PASSWORD == actualEncrptyedPassword
    }

    void testDecryption(){

        when:
        def actualDecryptedPassword = PasswordEncryptionUtil.decryptBase64(ENCRYPTED_PASSWORD, encryptionPassword, true)

        then:
        DECRYPTED_PASSWORD == actualDecryptedPassword
    }
}
