package de.avalstandard.oauth.demo;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.spi.HttpFacade.Request;
import org.keycloak.common.constants.GenericConstants;
import org.keycloak.common.util.FindFile;

/**
 * Ordnet einem URL-Pfad eine Keycloak-Config-Datei zu. Dient der Simulation unterschiedlicher Clients und Aspekte.
 *
 * @author Robert Nagel
 */
public class PathBasedKeycloakConfigResolver implements KeycloakConfigResolver {

  private static Map<String, String> prefix2ConfigFilenameMapper = new ConcurrentHashMap<>();

  public static void setPrefix2ConfigFilenameMap(Map<String, String> prefix2ConfigFilenameMap) {
    if (prefix2ConfigFilenameMap == null) {
      prefix2ConfigFilenameMapper = new ConcurrentHashMap<>();
    } else {
      prefix2ConfigFilenameMapper = new ConcurrentHashMap<>(prefix2ConfigFilenameMap);
    }
  }

  private ConcurrentHashMap<String, KeycloakDeployment> cachePath2Deployment = new ConcurrentHashMap<>();

  @Override
  public KeycloakDeployment resolve(Request facade) {
    String relPath = facade.getRelativePath();

    KeycloakDeployment keycloakDeployment = this.cachePath2Deployment.get(relPath);
    if (keycloakDeployment != null) {
      return keycloakDeployment;
    }

    InputStream isConfig = null;

    String longestPrefix = "";
    for (String prefix : prefix2ConfigFilenameMapper.keySet()) {
      if (relPath.startsWith(prefix)) {
        if (longestPrefix.length() < prefix.length()) {
          longestPrefix = prefix;
        }
      }
    }

    if (!"".equals(longestPrefix)) {
      String filename = prefix2ConfigFilenameMapper.get(longestPrefix);
      isConfig = FindFile.findFile(GenericConstants.PROTOCOL_CLASSPATH + filename);
    }

    if (isConfig == null) {
      isConfig = FindFile.findFile(GenericConstants.PROTOCOL_CLASSPATH + "aval-oauth-demo-github-unittest.config.json");
    }

    keycloakDeployment = KeycloakDeploymentBuilder.build(isConfig);

    this.cachePath2Deployment.put(relPath, keycloakDeployment);

    return keycloakDeployment;
  }
}
