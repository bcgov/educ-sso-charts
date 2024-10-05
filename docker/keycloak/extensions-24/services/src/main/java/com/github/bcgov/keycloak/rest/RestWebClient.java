package com.github.bcgov.keycloak.rest;

import com.github.bcgov.keycloak.common.properties.ApplicationProperties;
import com.github.bcgov.keycloak.util.LogHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.netty.http.client.HttpClient;

/**
 * The type Rest web client.
 */
@Configuration
@ComponentScan(basePackages={"com.github.bcgov.keycloak"})
public class RestWebClient {
  private final DefaultUriBuilderFactory factory;
  private final ClientHttpConnector connector;
  /**
   * The Props.
   */
  private final ApplicationProperties props = new ApplicationProperties();

  public RestWebClient() {
    this.factory = new DefaultUriBuilderFactory();
    this.factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
    final HttpClient client = HttpClient.create().compress(true);
    client.warmup()
      .block();
    this.connector = new ReactorClientHttpConnector(client);
  }

  /**
   * Web client web client.
   *
   * @return the web client
   */
  @Bean
  public WebClient webClient() {
    InMemoryReactiveClientRegistrationRepository clientRegistryRepo = new InMemoryReactiveClientRegistrationRepository(ClientRegistration
      .withRegistrationId(this.props.getClientID())
      .tokenUri(this.props.getTokenURL())
      .clientId(this.props.getClientID())
      .clientSecret(this.props.getClientSecret())
      .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
      .build());
    InMemoryReactiveOAuth2AuthorizedClientService clientService = new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistryRepo);
    AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager =
      new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistryRepo, clientService);
    ServerOAuth2AuthorizedClientExchangeFilterFunction oauthFilter = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
    oauthFilter.setDefaultClientRegistrationId(this.props.getClientID());
    return WebClient.builder()
      .defaultHeader("X-Client-Name", ApplicationProperties.SOAM)
      .codecs(configurer -> configurer
        .defaultCodecs()
        .maxInMemorySize(100 * 1024 * 1024))
      .filter(this.log())
      .clientConnector(this.connector)
      .uriBuilderFactory(this.factory)
      .filter(oauthFilter)
      .build();
  }

  private ExchangeFilterFunction log() {
    return (clientRequest, next) ->
      next
        .exchange(clientRequest)
        .doOnNext((clientResponse -> LogHelper.logClientHttpReqResponseDetails(clientRequest.method(), clientRequest.url().toString(), clientResponse.statusCode() != null ? clientResponse.statusCode().value() : 400, clientRequest.headers().get(ApplicationProperties.CORRELATION_ID))));
  }
}
