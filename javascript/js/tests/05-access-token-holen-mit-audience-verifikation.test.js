const oAuth2 = require('../modules/o-auth2.js');
const cfgDefaults = require('../config/defaults.json');

test('Access Token holen mit axios mit audience und anschließende audience Prüfung', async () => {
    let successfulOperation;
    const getAccessTokenResult = await oAuth2.tryGetAccessTokenWithAxios(true);
    if (getAccessTokenResult.success === true) {
        successfulOperation = await oAuth2.verifyAccessTokenAudience(getAccessTokenResult.accessToken, cfgDefaults.sampleAudience);
    } else
        successfulOperation = false;
    expect(successfulOperation).toBe(true);
});

test('Access Token holen mit axios mit audience und anschließende Verifikation mit keycloak-connect', async () => {
    let successfulOperation;
    const getAccessTokenResult = await oAuth2.tryGetAccessTokenWithAxios(true);
    if (getAccessTokenResult.success === true) {
        successfulOperation = await oAuth2.verifyAccessTokenWithKeycloakConnect(getAccessTokenResult.accessTokenString);
    } else
        successfulOperation = false;
    expect(successfulOperation).toBe(true);
});

test('Access Token holen mit axios mit audience und anschließende Verifikation mit keycloak-connect, audience manuell prüfen', async () => {
    let successfulOperation;
    const getAccessTokenResult = await oAuth2.tryGetAccessTokenWithAxios(true);
    if (getAccessTokenResult.success === true) {
        successfulOperation = await oAuth2.verifyAccessTokenWithKeycloakConnect(getAccessTokenResult.accessTokenString);
        if (successfulOperation === true) {
            successfulOperation = await oAuth2.verifyAccessTokenAudience(getAccessTokenResult.accessToken, cfgDefaults.sampleAudience);
        }
    } else
        successfulOperation = false;
    expect(successfulOperation).toBe(true);
});