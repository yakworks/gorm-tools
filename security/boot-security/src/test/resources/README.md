## Generating keys

What is checked in to github here is for testing only. Dont deploy with these
on a public facing site, not even on dev.

### EC ES256

```bash
#generate key pair for ES256
openssl ecparam -name prime256v1 -genkey -noout -out es256-key-pair.pem
# generate public
openssl ec -in es256-key-pair.pem -pubout -out es256-public.pem

# IF TESTING ON jwt.io YOU NEED TO ASO EXPORT THE PRIVATE KEY FROM KEYPAIR using the following
# and then can paste that in for testing
openssl pkcs8 -topk8 -inform pem -in es256-key-pair.pem -outform pem -nocrypt -out es256-private.pem

```

also see `TokenUtils.generateES256Key()` to do it in code. 

### RS256

see https://gist.github.com/ygotthilf/baa58da5c3dd1f69fae9

```bash
# -N '' tells it not to add passphrase, dont add one if asked
ssh-keygen -t rsa -b 2048 -m PEM -N '' -f jwtRS256.key

openssl rsa -in jwtRS256.key -pubout -outform PEM -out jwtRS256.key.pub

cat jwtRS256.key
cat jwtRS256.key.pub

```

also see `TokenUtils.generateRsaKey()` to do it in code. 

## Alt

see here for single `https://connect2id.com/products/nimbus-jose-jwt/openssl-key-generation`
