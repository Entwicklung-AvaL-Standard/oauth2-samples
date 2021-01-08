package de.avalstandard.oauth.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OAuthDemo {

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

    ObjectMapper jsonOM = new ObjectMapper();
    configOAuthMap = jsonOM.readValue(configOAuthString, Map.class);
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
  public void test0001_AccessToken_mit_Spring_RestTemplate() throws JsonParseException, JsonMappingException, IOException {

    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("client_id", configOAuthMap.get("resource").toString());
    formData.add("client_secret", ((Map<?, ?>) configOAuthMap.get("credentials")).get("secret").toString());
    formData.add("grant_type", "client_credentials");

    String tokenUrl = configOAuthMap.get("auth-server-url") + "/realms/" + configOAuthMap.get("realm")
        + "/protocol/openid-connect/token";

    String result = postFormToURL(formData, tokenUrl);
    assertNotNull(result);

    ObjectMapper jsonOM = new ObjectMapper();

    Map jsonResult = jsonOM.readValue(result, Map.class);
    assertTrue("kein access_token-Key enthalten", jsonResult.containsKey("access_token"));

    String accessTokenString = jsonResult.get("access_token").toString();
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
  }
}
