
## Okta Saml
https://developer.okta.com/blog/2022/08/05/spring-boot-saml


= JWT Login Sample

This sample demonstrates how to accept JWTs without using a separate authorization server.

This approach is useful in REST APIs when a user would like to locally authenticate with a username and password and then use a JWT thereafter.

[[usage]]
To use the application, first run it:

```bash
./gradlew :servlet:spring-boot:java:jwt:login:bootRun
```

If you `POST` to the `/token` endpoint with the user `user/password`:

```bash
curl -XPOST user:password@localhost:8080/token
or
http POST admin:123@localhost:8080/token -v

http POST localhost:8080/api/login username=admin password=123

```

Then the application responds with something similar to the following:

```bash
eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJzZWxmIiwic3ViIjoidXNlciIsImV4cCI6MTYwNDA0MzA1MSwiaWF0IjoxNjA0MDA3MDUxfQ.yDF_JgSwl5sk21CF7AE1AYbYzRd5YYqe3MIgSWpgN0t2UqsjaaEDhmmICKizt-_0iZy8nkEpNnvgqv5bOHDhs7AXlYS1pg8dgPKuyfkhyVIKa3DhuGyb7tFjwJxHpr128BXf1Dbq-p7Njy46tbKsZhP5zGTjdXlqlAhR4Bl5Fxaxr7D0gdTVBVTlUp9DCy6l-pTBpsvHxShkjXJ0GHVpIZdB-c2e_K9PfTW5MDPcHekG9djnWPSEy-fRvKzTsyVFhdy-X3NXQWWkjFv9bNarV-bhxMlzqhujuaeXJGEqUZlkhBxTsqFr1N7XVcmhs3ECdjEyun2fUSge4BoC7budsQ
```

So, next, request the token and export it:

```bash
export TOKEN=`curl -XPOST admin:123@localhost:8080/token`

TOKEN=`http POST admin:123@localhost:8080/token -b`
echo "$TOKEN"
http localhost:8080 -A bearer -a "$TOKEN"
```

Finally, request `/`, including the bearer token for authentication:

```bash
curl -H "Authorization: Bearer $TOKEN" localhost:8080
or
http localhost:8080 -A bearer -a "$TOKEN"
```

You should see a response like:

```bash
Hello, user!
```
