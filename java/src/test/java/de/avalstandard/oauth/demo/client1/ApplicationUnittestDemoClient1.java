package de.avalstandard.oauth.demo.client1;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.event.EventListener;

import de.avalstandard.oauth.demo.OAuthDemoKeycloakSecurityConfig;

@SpringBootApplication
// hier wird von <? extends WebSecurityConfigurerAdapter> abgeleitet, damit die Config auch geladen wird
public class ApplicationUnittestDemoClient1 extends OAuthDemoKeycloakSecurityConfig {

  public static AtomicInteger CLIENT1_HTTP_PORT = new AtomicInteger(0);

  public static void main(String[] args) {
    SpringApplication.run(ApplicationUnittestDemoClient1.class, args);
  }

  @EventListener
  public void onWebServerInitialized(final ServletWebServerInitializedEvent event) {
    /* Wir lassen uns an einen zufaelligen Port binden (application.properties), brauchen den aber dann letztlich in unseren Tests.
     *
     * [1] ...
     * 9.3.5. Discover the HTTP Port at Runtime
     * You can access the port the server is running on from log output or from the WebServerApplicationContext through its WebServer
     * The best way to get that and be sure it has been initialized is to add a @Bean of type
     * ApplicationListener<WebServerInitializedEvent> and pull the container out of the event when it is published.
     *
     * [1] https://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#howto-user-a-random-unassigned-http-port
     * [2] https://www.baeldung.com/spring-boot-running-port#2-handling-servletwebserverinitializedevent
     */
    CLIENT1_HTTP_PORT.set(event.getWebServer().getPort());
  }
}
