package yakworks.security

import org.springframework.http.HttpStatus

import grails.testing.mixin.integration.Integration
import spock.lang.Specification

import yakworks.rest.client.OkAuth
import yakworks.rest.client.OkHttpRestTrait

@Integration
class JwtDecodeSpec extends Specification implements OkHttpRestTrait {

    //default has an iss of https://yak.works
    static String ES256_DEFAULT_JWT="eyJhbGciOiJFUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlzcyI6Imh0dHBzOi8veWFrLndvcmtzIiwiaWF0IjoxNTE2MjM5MDIyfQ.4hTzyLGvRpyXmOVZh0Pe9qT1zCFA9wyQxjsRa_6GpDNwsPpVQQq0qZHEKo4xsdzY712ivugx-ViwFXrTeJAvfQ"
    //iss https://test-rs.com
    static String TEST_RS256_JWT="eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlzcyI6Imh0dHBzOi8vdGVzdC1ycy5jb20iLCJpYXQiOjE1MTYyMzkwMjJ9.PDyK0-HW0Z8MyeG1R52LbqtW9_JJWOkhVVDivs7SHRFHk0N0v_a5UypxpKK0pfscYeJjL8nnZCARnXR0m7VSLhQcCmPdkqiTY0u-8t5xJkEeRCwmk4Ze54LCR2XGuwsLX4qfulBOTHKTnmbPZGrNdShHPgSkvZXAD2Cog-lo7-rd6KwbE2e3MTQaAo4ufNpOo_hB10Am17LOSrMZ8lYpeH6tGz3pT-aqiXYpyEqjs59SOndsA6NSCmTTIHDv7UcfdSeSbIHFSdu7gOBymDKuMIviYcYSNI7IFj6L2poYQH8kvLlE4ViHMyGogLputmuQkBSCJ8SwmkimkmpHI8DJcw"
    //iss https://test-es.com
    static String TEST_ES256_JWT="eyJhbGciOiJFUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlzcyI6Imh0dHBzOi8vdGVzdC1lcy5jb20iLCJpYXQiOjE1MTYyMzkwMjJ9.BaCKYQCcV_ZUcZ8Fkxt9-Iw4xNC0j_HFVePHuJc3Fm2aj2AZY_VLfUfytIg6RYdx6c9W_3b457j0Ia3-zqna3Q"

    String endpoint = "/api/validate"

    void cleanupSpec() {
        OkAuth.TOKEN = null
    }

    void "test ES256_DEFAULT_JWT"() {
        setup:
        OkAuth.TOKEN = ES256_DEFAULT_JWT

        when:
        def resp = get("/api/validate")

        then:
        resp.code() == HttpStatus.OK.value()
    }

    void "test TEST_RS256_JWT"() {
        setup:
        OkAuth.TOKEN = TEST_RS256_JWT

        when:
        def resp = get("/api/validate")

        then:
        resp.code() == HttpStatus.OK.value()
    }

    void "test TEST_ES256_JWT"() {
        setup:
        OkAuth.TOKEN = TEST_ES256_JWT

        when:
        def resp = get("/api/validate")

        then:
        resp.code() == HttpStatus.OK.value()
    }

}
