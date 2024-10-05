package com.github.bcgov.keycloak.rest;


import com.github.bcgov.keycloak.common.properties.ApplicationProperties;
import com.github.bcgov.keycloak.model.SoamLoginEntity;
import com.github.bcgov.keycloak.model.SoamServicesCard;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.UUID;

/**
 * This class is used for REST calls
 *
 * @author Marco Villeneuve
 */
public class SoamRestUtils {

  private static Logger logger = Logger.getLogger(SoamRestUtils.class);

  private static ApplicationProperties props;

  private RestWebClient restWebClient;

  public SoamRestUtils() {
      this.restWebClient = new RestWebClient();
      props = new ApplicationProperties();
  }

  public void performLogin(String identifierType, String identifierValue, String userID, SoamServicesCard servicesCard) {
    String url = props.getSoamApiURL() + "/login";
    final String correlationID = logAndGetCorrelationID(identifierValue, url, HttpMethod.POST.toString());
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.add("correlationID", correlationID);
    MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
    map.add("identifierType", identifierType);
    map.add("identifierValue", identifierValue);
    map.add("userID", userID);

    if (servicesCard != null) {
      map.add("birthDate", servicesCard.getBirthDate());
      map.add("did", servicesCard.getDid());
      map.add("email", servicesCard.getEmail());
      map.add("gender", servicesCard.getGender());
      map.add("givenName", servicesCard.getGivenName());
      map.add("givenNames", servicesCard.getGivenNames());
      map.add("identityAssuranceLevel", servicesCard.getIdentityAssuranceLevel());
      map.add("postalCode", servicesCard.getPostalCode());
      map.add("surname", servicesCard.getSurname());
      map.add("userDisplayName", servicesCard.getUserDisplayName());
    }

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

    try {
      this.restWebClient.webClient().post()
              .uri(url)
              .headers(httpHeadersOnWebClientBeingBuilt -> httpHeadersOnWebClientBeingBuilt.addAll( headers ))
              .body(Mono.just(request), HttpEntity.class)
              .retrieve()
              .bodyToMono(SoamLoginEntity.class)
              .block();
    } catch (final HttpClientErrorException e) {
      throw new RuntimeException("Could not complete login call: " + e.getMessage());
    }
  }

  public SoamLoginEntity getSoamLoginEntity(String identifierType, String identifierValue) {
    String url = props.getSoamApiURL() + "/" + identifierType + "/" + identifierValue;
    final String correlationID = logAndGetCorrelationID(identifierValue, url, HttpMethod.GET.toString());
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.add("correlationID", correlationID);
    try {
      return this.restWebClient.webClient().get()
              .uri(url)
              .headers(httpHeadersOnWebClientBeingBuilt -> httpHeadersOnWebClientBeingBuilt.addAll( headers ))
              .retrieve()
              .bodyToMono(SoamLoginEntity.class)
              .block();
    } catch (final HttpClientErrorException e) {
      throw new RuntimeException("Could not complete getSoamLoginEntity call: " + e.getMessage());
    }
  }

  private String logAndGetCorrelationID(String identifierValue, String url, String httpMethod) {
    final String correlationID = UUID.randomUUID().toString();
    MDC.put("correlation_id", correlationID);
    MDC.put("user_guid", identifierValue);
    MDC.put("client_http_request_url", url);
    MDC.put("client_http_request_method", httpMethod);
    logger.info("correlation id for guid=" + identifierValue + " is=" + correlationID);
    MDC.clear();
    return correlationID;
  }
}
