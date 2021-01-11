package de.avalstandard.oauth.demo.client1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.avalstandard.oauth.demo.OAuthDemoKeycloakSecurityConfig;

@SpringBootApplication
// hier wird von <? extends WebSecurityConfigurerAdapter> abgeleitet, damit die Config auch geladen wird
public class ApplicationUnittestDemoClient1 extends OAuthDemoKeycloakSecurityConfig {

  public static void main(String[] args) {
    SpringApplication.run(ApplicationUnittestDemoClient1.class, args);
  }
}
