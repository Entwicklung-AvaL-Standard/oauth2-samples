package de.avalstandard.oauth.demo.client1;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.keycloak.util.JsonSerialization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControllerDemoClient1 {

  public static AtomicBoolean controllerInitialized = new AtomicBoolean(false);

  @Autowired
  WebSecurityConfigurer<WebSecurity> config;

  @GetMapping("/demo-client1/audience-verification/adapter/echo")
  public void audience_verification_adapter_echo(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Map<String, Object> jsonMap = new HashMap<>();
    jsonMap.put("function", "audience_verification_adapter_echo()");

    putAudiencesToMap(request, jsonMap);

    JsonSerialization.writeValuePrettyToStream(response.getOutputStream(), jsonMap);
  }

  @GetMapping("/demo-client1/audience-verification/manual/echo")
  public void audience_verification_manual_echo(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    Map<String, Object> jsonMap = new HashMap<>();
    jsonMap.put("function", "audience_verification_manual_echo()");

    putAudiencesToMap(request, jsonMap);

    JsonSerialization.writeValuePrettyToStream(response.getOutputStream(), jsonMap);
  }

  @EventListener
  private void onApplicationEvent(ContextRefreshedEvent event) {
    controllerInitialized.set(true);
  }

  private void putAudiencesToMap(HttpServletRequest request, Map<String, Object> jsonMap) throws IOException {
    RefreshableKeycloakSecurityContext rksc = (RefreshableKeycloakSecurityContext) request
        .getAttribute(KeycloakSecurityContext.class.getName());

    AccessToken accessToken = rksc.getToken();
    String[] audiences = accessToken.getAudience();

    List<String> audList = Arrays.asList(audiences);
    jsonMap.put("expected-audience", rksc.getDeployment().getResourceName());
    jsonMap.put("token-audiences", audList);
    jsonMap.put("audience-valid", audList.contains(rksc.getDeployment().getResourceName()));
  }
}