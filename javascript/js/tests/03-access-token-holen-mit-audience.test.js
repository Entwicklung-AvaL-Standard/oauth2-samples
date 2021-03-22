const oAuth2 = require('../modules/o-auth2.js');

test('Access Token holen mit axios und audience', async () => {
    const getAccessTokenResult = await oAuth2.tryGetAccessTokenWithAxios(true);
    expect(getAccessTokenResult.success).toBe(true);
});