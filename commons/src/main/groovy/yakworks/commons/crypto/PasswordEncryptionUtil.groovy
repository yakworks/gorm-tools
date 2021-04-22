/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.crypto

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.bouncycastle.crypto.engines.BlowfishEngine
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher
import org.bouncycastle.crypto.params.KeyParameter

/**
 * this is used for Base64 basic password hiding in our configs, for example for emails passwords in EmailerSetup
 * same basic hiding that kubernetes uses for its secrets.
 */
@CompileStatic
@Slf4j
class PasswordEncryptionUtil {

    /**
     * Encrypts or decrypts a byte array.
     *
     * @param data Data to be encrypted or decrypted
     * @param password Password to use
     * @param encrypting Are we encrypting or decrypting?
     *
     */
    static byte[] process(byte[] data, String password, boolean encrypting = true) {
        BlowfishEngine engine = new BlowfishEngine()
        KeyParameter key = new KeyParameter(password.getBytes())

        engine.init(encrypting, key)
        PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(engine)
        cipher.init(encrypting, key)

        byte[] outBytes = new byte[cipher.getOutputSize(data.length)]
        int len = cipher.processBytes(data, 0, data.length, outBytes, 0)
        cipher.doFinal(outBytes, len)
        return outBytes
    }

    /**
     * Encrypts a byte array and returns the result
     */
    static byte[] encrypt(byte[] data, String password) {
        return this.process(data, password, true)
    }

    /**
     * Decrypts a byte array and returns the result. In case of an error,
     * it returns null
     */
    static byte[] decrypt(byte[] data, String password) {
        byte[] result = this.process(data, password, false)
        return result
    }

    /**
     * Encrypts a byte array and returns a base64-encoded string
     *
     */
    static String encryptBase64(byte[] data, String password) {
        byte[] result = encrypt(data, password)
        return result.encodeBase64().toString()
    }

    /**
     * Encrypts a string and returns the result base64-encoded
     */
    static String encryptBase64(String data, String password) {
        return encryptBase64(data.getBytes(), password)
    }

    /**
     * Decrypts a base64-encoded string and returns the result. In case of an
     * error, it returns null
     */
    static String decryptBase64(String data, String password, boolean trim = true) {
        byte[] result = decrypt(data.decodeBase64(), password)
        String str
        if (result) {
            str = new String(result)
            if (trim) str = str.trim()
        }
        return str
    }
}
