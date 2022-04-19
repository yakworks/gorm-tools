package nine.security.oauth

import groovy.transform.CompileStatic

import org.pac4j.core.profile.creator.ProfileCreator
import org.pac4j.core.util.CommonHelper
import org.pac4j.oidc.client.OidcClient
import org.pac4j.oidc.config.OidcConfiguration
import org.pac4j.oidc.credentials.OidcCredentials
import org.pac4j.oidc.profile.OidcProfileDefinition
import org.pac4j.oidc.profile.creator.OidcProfileCreator

/**
 * This is the client for Oauth/OpenId with Okta.
 * Spring security rest expects client class to have methods setClientId/setSecret etc so custom client class implementation is required.
*/
@CompileStatic
class OktaClient extends OidcClient<OktaProfile, OidcConfiguration>{

    OktaClient() {
        super(new OidcConfiguration())
    }

    OktaClient(final OidcConfiguration configuration) {
        super(configuration);
    }

    void setClientId(final String id) {
        configuration.setClientId(id)
    }

    void setSecret(final String sc) {
        configuration.setSecret(sc)
    }

    void setDiscoveryUrl(final String u) {
        configuration.setDiscoveryURI(u)
    }

    // public void setCallbackUrl(final String url) {
    //     super.setCallbackUrl(url)
    // }

    @Override
    protected void clientInit() {
        CommonHelper.assertNotNull("configuration", getConfiguration())
        final OidcProfileCreator<OktaProfile> profileCreator = new OidcProfileCreator<>(getConfiguration())
        profileCreator.setProfileDefinition(new OidcProfileDefinition<>({ def x -> new OktaProfile()}))
        defaultProfileCreator(profileCreator as ProfileCreator<OidcCredentials, OktaProfile>)
        super.clientInit()
    }
}
