package com.github.bcgov.keycloak.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bcgov.keycloak.authenticators.SoamFirstTimeLoginAuthenticator;
import org.jboss.logging.Logger;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LogHelper {
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final String EXCEPTION = "Exception ";
  private static Logger logger = Logger.getLogger(LogHelper.class);

  private LogHelper() {

  }


  public static void logClientHttpReqResponseDetails(@NonNull final HttpMethod method, final String url, final int responseCode, final List<String> correlationID) {
    try {
      final Map<String, Object> httpMap = new HashMap<>();
      httpMap.put("client_http_response_code", responseCode);
      httpMap.put("client_http_request_method", method.toString());
      httpMap.put("client_http_request_url", url);
      if (correlationID != null) {
        httpMap.put("correlation_id", String.join(",", correlationID));
      }
      MDC.putCloseable("httpEvent", mapper.writeValueAsString(httpMap));
      logger.info("");
      MDC.clear();
    } catch (final Exception exception) {
      logger.error(EXCEPTION, exception);
    }
  }

}
