package yakworks.commons.crypto

import spock.lang.Specification

class PasswordEncryptionUtilSpec extends Specification {

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
