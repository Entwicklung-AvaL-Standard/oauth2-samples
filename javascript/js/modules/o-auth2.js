const cfgKeycloak = require('../config/keycloak.json');
const cfgDefaults = require('../config/defaults.json');
const KeycloakConnect = require('keycloak-connect');
const axiosRequest = require('./axiosRequestWrapper.js');
const qs = require('qs');

async function tryGetAccessTokenWithAxios(withAudience) {
    let reqBody;
    if (withAudience === true) {
        reqBody = qs.stringify({
            "grant_type": "urn:ietf:params:oauth:grant-type:uma-ticket",
            "client_id": `${cfgKeycloak.resource}`,
            "client_secret": `${cfgKeycloak.credentials.secret}`,
            "audience": `${cfgDefaults.sampleAudience}`
        });
    } else {
        reqBody = qs.stringify({
            "grant_type": "client_credentials",
            "client_id": `${cfgKeycloak.resource}`,
            "client_secret": `${cfgKeycloak.credentials.secret}`
        });
    }

    let promise;
    try {
        promise = await axiosRequest({
            method: 'post',
            url: `${cfgKeycloak["auth-server-url"]}realms/${cfgKeycloak.realm}/protocol/openid-connect/token`,
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                "Accept": "*/*"
            },
            content: reqBody
        });
        if (typeof promise.response != 'undefined') {
            let accessTokenString = promise.response.body.access_token.toString();
            if (accessTokenString !== '') {
                console.log('Receiving Access Token from given Client Credentials with axios was successful.');
                return {
                    success: true,
                    accessToken: await createAccessTokenObject(accessTokenString),
                    accessTokenString: accessTokenString
                };
            } else {
                return {success: false};
            }
        }
    } catch (e) {
        console.error(`Obtaining Access Token from given Client Credentials with axios was not possible.`);
        if (e.status == '404') {
            console.error(`Reason: OAuth2 Server not found.`);
        } else if (e.status == '400') {
            if (typeof e.data.error != 'undefined') {
                console.error(`Reason: ${e.data.error.toString()}`);
            }
        } else {
            if (typeof e.status != 'undefined' && typeof e.statusText != 'undefined') {
                console.error(`Reason: ${e.statusText.toString()}`);
            }
        }
        return {success: false};
    }
}

async function createAccessTokenObject(accessTokenString) {
    let [header, token, signature] = accessTokenString.split('.');
    return JSON.parse(Buffer.from(token, 'base64').toString('ascii'));
}

async function tryGetAccessTokenWithKeycloakConnect(accessTokenString) {
    let promise;
    let keycloakConnect = new KeycloakConnect(cfgKeycloak, cfgKeycloak);
    try {
        promise = await keycloakConnect.grantManager.obtainFromClientCredentials();
        if (typeof promise != 'undefined') {
            accessTokenString = promise.access_token.token.toString();
            if (accessTokenString !== '') {
                console.log('Receiving Access Token from given Client Credentials with keycloak-connect was successful.');
                return {success: true, accessTokenString: accessTokenString, accessToken: promise.access_token};
            } else {
                return {success: false};
            }
        }
    } catch (e) {
        console.error(`Obtaining Access Token from given Client Credentials with keycloak-connect was not possible.`);
        return {success: false};
    }
}

async function verifyAccessTokenAudience(accessToken, expectedAudience) {
    if (Array.isArray(accessToken.aud)) {
        return typeof accessToken.aud.find(value => {
            return value === expectedAudience;
        }) != 'undefined';
    } else {
        return accessToken.aud === expectedAudience;
    }
}

async function verifyAccessTokenWithKeycloakConnect(accessTokenString) {
    let keycloakConnect = new KeycloakConnect(cfgKeycloak, cfgKeycloak);
    let result = await keycloakConnect.grantManager.validateAccessToken(accessTokenString);
    if (result === false) {
        console.error(`Access Token could not be validated.`);
        return false;
    } else return typeof result === typeof accessTokenString;
}

module.exports = {
    tryGetAccessTokenWithAxios,
    tryGetAccessTokenWithKeycloakConnect,
    verifyAccessTokenWithKeycloakConnect,
    verifyAccessTokenAudience
}