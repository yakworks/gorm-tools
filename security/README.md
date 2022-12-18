# Spring Security

- `org.yakworks:security-core` - Dependency free, Simple facade interfaces and impls for User/Subject and Roles/Permissions
- `org.yakworks:boot-security` - Spring Security with setup for basic, saml2, oauth. Depends only on spring boot and security
- `org.yakworks:boot-security-gorm` - gorm entities and support for Users/Roles

when adding to a project may also need to add 
`implementation "org.springframework.boot:spring-boot-starter-oauth2-resource-server:$springBootVersion"`

## Gen RSA key pairs
https://gist.github.com/ygotthilf/baa58da5c3dd1f69fae9

## ES256 Keys
https://notes.salrahman.com/generate-es256-es384-es512-private-keys/
https://connect2id.com/products/nimbus-jose-jwt/openssl-key-generation
https://gist.github.com/lbalmaceda/9a0c7890c2965826c04119dcfb1a5469
https://www.scottbrady91.com/jose/jwts-which-signing-algorithm-should-i-use
https://www.scottbrady91.com/openssl/creating-elliptical-curve-keys-using-openssl

```bash
#generate key pair
openssl ecparam -name prime256v1 -genkey -noout -out es256-key-pair.pem
# generate public
openssl ec -in es256-key-pair.pem -pubout -out es256-public.pem
# generate private pem only (works in jwt.io)
openssl pkcs8 -topk8 -inform pem -in es256-key-pair.pem -outform pem -nocrypt -out es256-private.pem

```

