/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.token

import java.security.KeyFactory
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.EncodedKeySpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

import groovy.transform.CompileStatic

import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemReader
import org.springframework.core.io.Resource

/**
 * helpers to generate key pairs, RSA right now.
 */
@CompileStatic
class PemUtils {

    static KeyPair parseKeyPair(Resource resource) {
        // Load BouncyCastle as JCA provider
        //Security.addProvider(new BouncyCastleProvider());

        // Parse the EC key pair
        PEMParser pemParser = new PEMParser(new InputStreamReader(resource.getInputStream()))
        PEMKeyPair pemKeyPair = (PEMKeyPair) pemParser.readObject()

        // Convert to Java (JCA) format
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter()
        KeyPair keyPair = converter.getKeyPair(pemKeyPair)
        pemParser.close()
        return keyPair
    }

    private static byte[] parsePEMFile(Resource resource) throws IOException {
        if (!resource.exists()) {
            throw new FileNotFoundException(String.format("The file '%s' doesn't exist.", resource.filename));
        }
        PemReader reader = new PemReader(new InputStreamReader(resource.getInputStream()))
        PemObject pemObject = reader.readPemObject()
        byte[] content = pemObject.getContent()
        reader.close()
        return content
    }

    private static PublicKey getPublicKey(byte[] keyBytes, String algorithm) {
        PublicKey publicKey = null;
        KeyFactory kf = KeyFactory.getInstance(algorithm);
        EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        publicKey = kf.generatePublic(keySpec);

        return publicKey;
    }

    private static PrivateKey getPrivateKey(byte[] keyBytes, String algorithm) {
        PrivateKey privateKey = null;
        KeyFactory kf = KeyFactory.getInstance(algorithm);
        EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        privateKey = kf.generatePrivate(keySpec);

        return privateKey;
    }

    static PublicKey readPublicKeyFromFile(Resource resource, String algorithm) throws IOException {
        byte[] bytes = PemUtils.parsePEMFile(resource);
        return PemUtils.getPublicKey(bytes, algorithm);
    }

    static PrivateKey readPrivateKeyFromFile(Resource resource, String algorithm) throws IOException {
        byte[] bytes = PemUtils.parsePEMFile(resource);
        return PemUtils.getPrivateKey(bytes, algorithm);
    }
}
