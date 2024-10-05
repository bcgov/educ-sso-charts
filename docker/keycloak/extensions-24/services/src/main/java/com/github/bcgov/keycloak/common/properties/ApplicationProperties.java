package com.github.bcgov.keycloak.common.properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;
import org.springframework.stereotype.Component;

/**
 * Class holds all application properties
 *
 * @author Marco Villeneuve
 *
 */
@Component
public class ApplicationProperties {
  public static final ObjectMapper mapper = new ObjectMapper();
	private static Logger logger = Logger.getLogger(ApplicationProperties.class);
	public static final String CORRELATION_ID = "correlationID";
	public static final String SOAM = "SOAM";
	private String soamApiURL;
	private String tokenURL;
	private String clientID;
	private String clientSecret;

	public ApplicationProperties() {
		logger.info("SOAM: Building application properties");
		soamApiURL = System.getenv().getOrDefault("SOAMAPIURL", "MissingSoamURL");
		tokenURL = System.getenv().getOrDefault("TOKENURL", "MissingSoamTokenURL");
		clientID = System.getenv().getOrDefault("CLIENTID", "MissingSoamClientID");
		clientSecret = System.getenv().getOrDefault("CLIENTSECRET", "MissingSoamClientSecret");
	}

	public String getSoamApiURL() {
		return soamApiURL;
	}

	public String getTokenURL() {
		return tokenURL;
	}

	public String getClientID() {
		return clientID;
	}

	public String getClientSecret() {
		return clientSecret;
	}

}
