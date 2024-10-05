package com.github.bcgov.keycloak.tenant.rest;


import com.github.bcgov.keycloak.common.properties.ApplicationProperties;
import com.github.bcgov.keycloak.rest.RestWebClient;
import com.github.bcgov.keycloak.tenant.model.TenantAccess;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.UUID;

/**
 * This class is used for REST calls
 *
 * @author Marco Villeneuve
 */
public class TenantRestUtils {

  private static Logger logger = Logger.getLogger(TenantRestUtils.class);

  private static ApplicationProperties props;

  private RestWebClient restWebClient;

  public TenantRestUtils() {
    this.restWebClient = new RestWebClient();
    props = new ApplicationProperties();
  }

  public TenantAccess checkForValidTenant(String clientID, String tenantID) {
    String url = props.getSoamApiURL() + "/tenant?clientID=" + clientID + "&tenantID=" + tenantID;
    final String correlationID = logAndGetCorrelationID(tenantID, url, HttpMethod.GET.toString());
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.add("correlationID", correlationID);

    try {
      return this.restWebClient.webClient().get()
              .uri(url)
              .headers(httpHeadersOnWebClientBeingBuilt -> httpHeadersOnWebClientBeingBuilt.addAll( headers ))
              .retrieve()
              .bodyToMono(TenantAccess.class)
              .block();
    } catch (final HttpClientErrorException e) {
      throw new RuntimeException("Could not complete checkForValidTenant call: " + e.getMessage());
    }
  }

  private String logAndGetCorrelationID(String tenantID, String url, String httpMethod) {
    final String correlationID = UUID.randomUUID().toString();
    MDC.put("correlation_id", correlationID);
    MDC.put("tenant_id", tenantID);
    MDC.put("client_http_request_url", url);
    MDC.put("client_http_request_method", httpMethod);
    logger.info("correlation id for tenant ID=" + tenantID + " is=" + correlationID);
    MDC.clear();
    return correlationID;
  }
}
