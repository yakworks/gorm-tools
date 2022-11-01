# Spring Security

- `org.yakworks:security-core` - Dependency free, Simple facade interfaces and impls for User/Subject and Roles/Permissions
- `org.yakworks:boot-security` - Spring Security with setup for basic, saml2, oauth. Depends only on spring boot and security
- `org.yakworks:boot-security-gorm` - gorm entities and support for Users/Roles

when adding to a project may also need to add 
`implementation "org.springframework.boot:spring-boot-starter-oauth2-resource-server:$springBootVersion"`


