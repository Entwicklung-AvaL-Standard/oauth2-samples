package de.avalstandard.oauth.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.rotation.AdapterTokenVerifier;
import org.keycloak.adapters.rotation.AdapterTokenVerifier.VerifiedTokens;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.idm.authorization.AuthorizationRequest;
import org.keycloak.representations.idm.authorization.AuthorizationResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OAuthDemo {

  private static ObjectMapper jsonOM = new ObjectMapper();
  private static String configOAuthString = null;
  private static Map<String, Object> configOAuthMap = null;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    // Woher die config genau kommt ist eigentlich egal
    InputStream configUnittestOAuthClient = ClassLoader.getSystemResourceAsStream("aval-oauth-demo-github-unittest.config.json");
    InputStreamReader isr = new InputStreamReader(configUnittestOAuthClient, StandardCharsets.UTF_8);

    StringBuilder sb = new StringBuilder();
    char[] buf = new char[2048];
    for (int charsRead = isr.read(buf); charsRead != -1; charsRead = isr.read(buf)) {
      sb.append(buf, 0, charsRead);
    }
    configOAuthString = sb.toString();

    configOAuthMap = jsonOM.readValue(configOAuthString, Map.class);
  }

  private void checkAccessTokenString(String accessTokenString, String expectedAudience, KeycloakDeployment keycloakDeployment)
      throws JsonParseException,
        JsonMappingException,
        IOException,
        VerificationException {
    String jwtParts[] = accessTokenString.split("\\.");
    String header_b64 = jwtParts[0];
    String content_b64 = jwtParts[1];
    String sig_b64 = jwtParts[2];

    Map<String, Object> accessTokenMap = jsonOM.readValue(Base64.getUrlDecoder().decode(content_b64), Map.class);
    // das Token ist fuer uns ausgestellt
    assertEquals(configOAuthMap.get("resource"), accessTokenMap.get("azp"));
    assertNotNull(accessTokenMap.get("iss"));
    // das Token ist von demjenigen ausgestellt, den wir befragt haben
    assertTrue(accessTokenMap.get("iss").toString().startsWith(configOAuthMap.get("auth-server-url").toString()));

    if (expectedAudience != null) {
      assertEquals(expectedAudience, accessTokenMap.get("aud"));
    }

    if (keycloakDeployment != null) {
      /*
       * prueft intern die JWT-Signatur inklusive Beschaffung der notwendigen PublicKeys vom Server. Daher ist auch ein solches
       * KeycloakDeployment notwendig, da dort die notwendigen URLs enthalten sind.
       */
      VerifiedTokens tokens = AdapterTokenVerifier.verifyTokens(accessTokenString, null, keycloakDeployment);
      if (tokens != null) {
        tokens = null;
      }
    }
  }

  private String postFormToURL(MultiValueMap<String, String> formData, String url) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.add("Accept", "*/*");

    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);
    RestTemplate rest = new RestTemplate();
    ResponseEntity<String> responseEntity = rest.exchange(url, HttpMethod.POST, requestEntity, String.class);
    String result = responseEntity.getBody();
    return result;
  }

  @Test
  public void test0000_config_ist_geladen() {
    assertNotNull(configOAuthString);
    assertNotNull(configOAuthMap);
    assertEquals("https://auth-test.avalstandard.de/auth/", configOAuthMap.get("auth-server-url"));
    assertEquals("avalstandard", configOAuthMap.get("realm"));
    assertNotNull(configOAuthMap.get("credentials"));
    assertNotNull(((Map<?, ?>) configOAuthMap.get("credentials")).get("secret"));
  }

  @Test
  public void test0001_AccessToken_mit_Spring_RestTemplate()
      throws JsonParseException,
        JsonMappingException,
        IOException,
        VerificationException {

    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("client_id", configOAuthMap.get("resource").toString());
    formData.add("client_secret", ((Map<?, ?>) configOAuthMap.get("credentials")).get("secret").toString());
    formData.add("grant_type", "client_credentials");

    String tokenUrl = configOAuthMap.get("auth-server-url") + "/realms/" + configOAuthMap.get("realm")
        + "/protocol/openid-connect/token";

    String result = postFormToURL(formData, tokenUrl);
    assertNotNull(result);

    Map jsonResult = jsonOM.readValue(result, Map.class);
    assertTrue("kein access_token-Key enthalten", jsonResult.containsKey("access_token"));

    String accessTokenString = jsonResult.get("access_token").toString();
    checkAccessTokenString(accessTokenString, null, null);
  }

  @Test
  public void test0001a_AccessToken_und_audience_mit_Spring_RestTemplate()
      throws JsonParseException,
        JsonMappingException,
        IOException,
        VerificationException {
    /* @formatter:off

    curl -X POST \
      http://${host}:${port}/auth/realms/${realm}/protocol/openid-connect/token \
      --data "grant_type=urn:ietf:params:oauth:grant-type:uma-ticket" \
      --data "client_id={resource_server_client_id}" \
      --data "client_secret={resource_server_client_secret}" \
      --data "audience={resource_server_client_id}"

       @formatter:on
     */

    String tokenUrl = configOAuthMap.get("auth-server-url") + "/realms/" + configOAuthMap.get("realm")
        + "/protocol/openid-connect/token";

    String desiredAudience = "aval-oauth-demo-github-unittest-demo-client-1";

    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("client_id", configOAuthMap.get("resource").toString());
    formData.add("client_secret", ((Map<?, ?>) configOAuthMap.get("credentials")).get("secret").toString());
    formData.add("audience", desiredAudience);

    String[] grantTypes = new String[] {
        OAuth2Constants.UMA_GRANT_TYPE, // "urn:ietf:params:oauth:grant-type:uma-ticket"
        OAuth2Constants.CLIENT_CREDENTIALS, // "client_credentials"
    };

    for (int i = 0; i < grantTypes.length; i++) {
      String grant_type = grantTypes[i];

      formData.remove(OAuth2Constants.GRANT_TYPE);
      formData.add(OAuth2Constants.GRANT_TYPE, grant_type);
      String result = postFormToURL(formData, tokenUrl);
      Map jsonResult = jsonOM.readValue(result, Map.class);
      String accessTokenString = jsonResult.get("access_token").toString();

      if (OAuth2Constants.CLIENT_CREDENTIALS.equals(grant_type)) {
        /*
         * Fuer AccessTokens mit grant_type=client_credentials vergibt der Keycloak nicht die geforderte Audience, sondern traegt
         * immer "account" ein.
         */
        checkAccessTokenString(accessTokenString, "account", null);
      } else if (OAuth2Constants.UMA_GRANT_TYPE.equals(grant_type)) {
        /*
         * Wenn das AccessToken mit mit grant_type=urn:ietf:params:oauth:grant-type:uma-ticket angefordert, dann traegt der
         * Keycloak-Server die gewuenscht Audience im "aud"-Claim ein.
         */
        checkAccessTokenString(accessTokenString, desiredAudience, null);
      } else {
        fail("unbehandelter grant_type");
      }
    }
  }

  @Test
  public void test0002_AccessToken_mit_Spring_OAuth2RestTemplate()
      throws JsonParseException,
        JsonMappingException,
        IOException,
        VerificationException {
    String tokenUrl = configOAuthMap.get("auth-server-url") + "/realms/" + configOAuthMap.get("realm")
        + "/protocol/openid-connect/token";

    ClientCredentialsResourceDetails ccrd = new ClientCredentialsResourceDetails();
    ccrd.setClientId(configOAuthMap.get("resource").toString());
    ccrd.setClientSecret(((Map<?, ?>) configOAuthMap.get("credentials")).get("secret").toString());
    ccrd.setAccessTokenUri(tokenUrl);

    // Create the RestTemplate and add a the Token Provider
    OAuth2RestTemplate oa2rt = new OAuth2RestTemplate(ccrd);
    OAuth2AccessToken oa2at = oa2rt.getAccessToken();
    String accessTokenString = oa2at.getValue();
    checkAccessTokenString(accessTokenString, null, null);
  }

  @Test
  public void test0003_AccessToken_mit_Keycloak_AuthzClient()
      throws JsonParseException,
        JsonMappingException,
        IOException,
        VerificationException {
    AuthzClient authzClient = AuthzClient.create(new ByteArrayInputStream(configOAuthString.getBytes(StandardCharsets.UTF_8)));
    /*
     * Erfordert, dass die Client-Konfiguration im Keycloak-Server auf "Access Type = confidential" und
     * "Authorization Enabled = ON" hat, da hier standardmaessig der grant_type=urn:ietf:params:oauth:grant-type:uma-ticket
     * verwendet wird.
     */
    AuthorizationResponse atr = authzClient.authorization().authorize();
    String accessTokenString = atr.getToken();
    checkAccessTokenString(accessTokenString, null, null);
  }

  @Test
  public void test0003a_AccessToken_und_audience_mit_Keycloak_AuthzClient()
      throws JsonParseException,
        JsonMappingException,
        IOException,
        VerificationException {
    String desiredAudience = "aval-oauth-demo-github-unittest-demo-client-1";

    AuthzClient authzClient = AuthzClient.create(new ByteArrayInputStream(configOAuthString.getBytes(StandardCharsets.UTF_8)));
    /*
     * Erfordert, dass die Client-Konfiguration im Keycloak-Server auf "Access Type = confidential" und
     * "Authorization Enabled = ON" hat, da hier standardmaessig der grant_type=urn:ietf:params:oauth:grant-type:uma-ticket
     * verwendet wird.
     */
    AuthorizationRequest authRequest = new AuthorizationRequest();
    authRequest.setAudience(desiredAudience);
    AuthorizationResponse atr = authzClient.authorization().authorize(authRequest);
    String accessTokenString = atr.getToken();
    checkAccessTokenString(accessTokenString, desiredAudience, null);
  }

  @Test
  public void test0004_AccessToken_mit_Keycloak_AuthzClient_und_CryptoVerification()
      throws JsonParseException,
        JsonMappingException,
        IOException,
        VerificationException {
    AuthzClient authzClient = AuthzClient.create(new ByteArrayInputStream(configOAuthString.getBytes(StandardCharsets.UTF_8)));
    /*
     * Erfordert, dass die Client-Konfiguration im Keycloak-Server auf "Access Type = confidential" und
     * "Authorization Enabled = ON" hat, da hier standardmaessig der grant_type=urn:ietf:params:oauth:grant-type:uma-ticket
     * verwendet wird.
     */
    AuthorizationResponse atr = authzClient.authorization().authorize();
    String accessTokenString = atr.getToken();
    KeycloakDeployment kcd = KeycloakDeploymentBuilder.build(authzClient.getConfiguration());
    checkAccessTokenString(accessTokenString, null, kcd);
  }
}
