package de.avalstandard.oauth.demo;

import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springsecurity.AdapterDeploymentContextFactoryBean;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.config.KeycloakSpringConfigResolverWrapper;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.session.HttpSessionEventPublisher;

/* https://www.keycloak.org/docs/latest/securing_apps/#_spring_security_adapter
 *
 */
@KeycloakConfiguration
@Configuration
public class OAuthDemoKeycloakSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {
  /**
   * Wir wollen einen PathBasedKeycloakConfigResolver verwenden, das konfigurieren wir hier.
   */
  @Override
  @Bean
  protected AdapterDeploymentContext adapterDeploymentContext() throws Exception {

    KeycloakConfigResolver keycloakConfigResolver = new PathBasedKeycloakConfigResolver();

    AdapterDeploymentContextFactoryBean factoryBean = new AdapterDeploymentContextFactoryBean(
        new KeycloakSpringConfigResolverWrapper(keycloakConfigResolver));
    factoryBean.afterPropertiesSet();
    return factoryBean.getObject();
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    super.configure(http);
    http
        .authorizeRequests()
        .antMatchers("/*").authenticated()
        .anyRequest().permitAll();
  }

  /**
   * Registers the KeycloakAuthenticationProvider with the authentication manager.
   */
  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    auth.authenticationProvider(keycloakAuthenticationProvider());
  }

  /*
   * https://www.keycloak.org/docs/latest/securing_apps/#_spring_security_adapter
   */
  @Bean
  public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher1() {
    return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
  }

  /**
   * Defines the session authentication strategy. You must provide a session authentication strategy bean which should be of type
   * RegisterSessionAuthenticationStrategy for public or confidential applications and NullAuthenticatedSessionStrategy for
   * bearer-only applications.
   */
  @Bean
  @Override
  protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
    return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
  }
}
