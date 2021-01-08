package de.avalstandard.oauth.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

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

  @Test
  public void test0000_config_ist_geladen() {
    assertNotNull(configOAuthString);
    assertNotNull(configOAuthMap);
    assertEquals("https://auth-test.avalstandard.de/auth/", configOAuthMap.get("auth-server-url"));
    assertEquals("avalstandard", configOAuthMap.get("realm"));
    assertNotNull(configOAuthMap.get("credentials"));
    assertNotNull(((Map<?, ?>) configOAuthMap.get("credentials")).get("secret"));
  }
}
