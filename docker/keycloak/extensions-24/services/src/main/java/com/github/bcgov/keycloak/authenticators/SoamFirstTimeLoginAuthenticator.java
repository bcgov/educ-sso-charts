package com.github.bcgov.keycloak.authenticators;


import com.github.bcgov.keycloak.common.utils.CommonUtils;
import com.github.bcgov.keycloak.exception.SoamRuntimeException;
import com.github.bcgov.keycloak.model.SoamServicesCard;
import com.github.bcgov.keycloak.rest.SoamRestUtils;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.JsonWebToken;

import java.util.List;
import java.util.Map;

/**
 * SOAM First Time login authenticator
 * This class will handle the callouts to our API
 *
 * @author Marco Villeneuve
 */
public class SoamFirstTimeLoginAuthenticator extends AbstractIdpAuthenticator {

  private static Logger logger = Logger.getLogger(SoamFirstTimeLoginAuthenticator.class);

  private SoamRestUtils soamRestUtils = new SoamRestUtils();


  @Override
  protected void actionImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
  }

  @Override
  protected void authenticateImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
    logger.debug("SOAM: inside authenticateImpl");
    KeycloakSession session = context.getSession();
    RealmModel realm = context.getRealm();

    if (context.getAuthenticationSession().getAuthNote(EXISTING_USER_INFO) != null) {
      context.attempted();
      return;
    }

    Map<String, Object> brokerClaims = brokerContext.getContextData();
    for (String s : brokerClaims.keySet()) {
      logger.debug("Context Key: " + s + " Value: " + brokerClaims.get(s));
    }

    String accountType = CommonUtils.getValueForAttribute("user.attributes.account_type", brokerContext);

    if (accountType == null) {
      throw new SoamRuntimeException("Account type is null; account type should always be available, check the IDP mappers for the hardcoded attribute");
    }

    String username = CommonUtils.getValueForAttribute("user.attributes.username", brokerContext);

    switch (accountType) {
      case "entra":
        logger.debug("SOAM: Account type entra found");
        if (username == null) {
          throw new SoamRuntimeException("No entra oid value was found in token");
        }
        createOrUpdateUser(CommonUtils.getValueForAttribute("user.attributes.entra_user_id", brokerContext), accountType, "ENTRA", null);
        break;
      case "bceidbasic":
        logger.debug("SOAM: Account type bceid found");
        if (username == null) {
          throw new SoamRuntimeException("No bceid_guid value was found in token");
        }
        createOrUpdateUser(CommonUtils.getValueForAttribute("user.attributes.bceid_guid", brokerContext), accountType, "BASIC", null);
        break;
      case "bcsc":
        logger.debug("SOAM: Account type bcsc found");

        if (username == null) {
          throw new SoamRuntimeException("No bcsc_did value was found in token");
        }

        SoamServicesCard servicesCard = new SoamServicesCard();
        servicesCard.setBirthDate(CommonUtils.getValueForAttribute("user.attributes.birthdate", brokerContext));
        servicesCard.setDid(CommonUtils.getValueForAttribute("user.attributes.did", brokerContext));
        servicesCard.setEmail(CommonUtils.getValueForAttribute("user.attributes.emailAddress", brokerContext));
        servicesCard.setGender(CommonUtils.getValueForAttribute("user.attributes.gender", brokerContext));
        servicesCard.setGivenName(CommonUtils.getValueForAttribute("user.attributes.given_name", brokerContext));
        servicesCard.setGivenNames(CommonUtils.getValueForAttribute("user.attributes.given_names", brokerContext));
        servicesCard.setIdentityAssuranceLevel(CommonUtils.getValueForAttribute("user.attributes.identity_assurance_level", brokerContext));
        servicesCard.setPostalCode(CommonUtils.getValueForAttribute("user.attributes.postal_code", brokerContext));
        servicesCard.setSurname(CommonUtils.getValueForAttribute("user.attributes.family_name", brokerContext));
        servicesCard.setUserDisplayName(CommonUtils.getValueForAttribute("user.attributes.display_name", brokerContext));
        createOrUpdateUser(servicesCard.getDid(), accountType, "BCSC", servicesCard);
        break;
      default:
        throw new SoamRuntimeException("Account type is not bcsc or bceid check IDP mappers");
    }

    if (context.getSession().users().getUserByUsername(realm, username) == null) {
      logger.debugf("No duplication detected. Creating account for user '%s' and linking with identity provider '%s' .",
        username, brokerContext.getIdpConfig().getAlias());

      UserModel federatedUser = session.users().addUser(realm, username);
      federatedUser.setEnabled(true);

      if (accountType.equals("bcsc")) {
        federatedUser.setSingleAttribute("user_did", CommonUtils.getValueForAttribute("user.attributes.did", brokerContext));
      }

      for (Map.Entry<String, List<String>> attr : serializedCtx.getAttributes().entrySet()) {
        federatedUser.setAttribute(attr.getKey(), attr.getValue());
      }

      context.setUser(federatedUser);
      context.getAuthenticationSession().setAuthNote(BROKER_REGISTERED_NEW_USER, "true");
      context.success();
    } else {
      logger.debug("SOAM: Existing " + accountType + " user found with username: " + username);
      UserModel existingUser = context.getSession().users().getUserByUsername(realm, username);
      context.setUser(existingUser);
      context.success();
    }
  }

  protected void createOrUpdateUser(String guid, String accountType, String credType, SoamServicesCard servicesCard) {
    logger.debug("SOAM: createOrUpdateUser");
    logger.debug("SOAM: performing login for " + accountType + " user: " + guid);

    try {
      soamRestUtils.performLogin(credType, guid, guid, servicesCard);
    } catch (Exception e) {
      logger.error("Exception occurred within SOAM while processing login" + e.getMessage());
      throw new SoamRuntimeException("Exception occurred within SOAM while processing login, check downstream logs for SOAM API service");
    }
  }


  @Override
  public boolean requiresUser() {
    return false;
  }

  @Override
  public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
    return true;
  }

}
