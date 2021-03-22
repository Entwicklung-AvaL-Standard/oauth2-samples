const oAuth2 = require('../modules/o-auth2.js');

test('Access Token holen mit keycloak-connect und anschließende Verifikation', async () => {
    let successfulOperation;
    const getAccessTokenResult = await oAuth2.tryGetAccessTokenWithKeycloakConnect();
    if (getAccessTokenResult.success === true) {
        successfulOperation = await oAuth2.verifyAccessTokenWithKeycloakConnect(getAccessTokenResult.accessTokenString);
    } else
        successfulOperation = false;
    expect(successfulOperation).toBe(true);
});
