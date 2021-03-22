const oAuth2 = require('../modules/o-auth2.js');

test('Access Token holen mit axios', async () => {
    const getAccessTokenResult = await oAuth2.tryGetAccessTokenWithAxios(false);
    expect(getAccessTokenResult.success).toBe(true);
});

test('Access Token holen mit keycloak-connect', async () => {
    const getAccessTokenResult = await oAuth2.tryGetAccessTokenWithKeycloakConnect();
    expect(getAccessTokenResult.success).toBe(true);
});