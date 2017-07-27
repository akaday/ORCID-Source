/**
 * =============================================================================
 *
 * ORCID (R) Open Source
 * http://orcid.org
 *
 * Copyright (c) 2012-2014 ORCID, Inc.
 * Licensed under an MIT-Style License (MIT)
 * http://orcid.org/open-source-license
 *
 * This copyright and license information (including a link to the full license)
 * shall be included in its entirety in all copies or substantial portion of
 * the software.
 *
 * =============================================================================
 */
package org.orcid.frontend.web.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.orcid.core.manager.ClientDetailsManager;
import org.orcid.core.manager.ClientManager;
import org.orcid.core.manager.OrcidSSOManager;
import org.orcid.core.manager.ProfileEntityCacheManager;
import org.orcid.core.manager.ProfileEntityManager;
import org.orcid.core.manager.read_only.ClientManagerReadOnly;
import org.orcid.core.manager.read_only.EmailManagerReadOnly;
import org.orcid.jaxb.model.clientgroup.RedirectUriType;
import org.orcid.jaxb.model.message.OrcidType;
import org.orcid.persistence.jpa.entities.ProfileEntity;
import org.orcid.pojo.ajaxForm.Client;
import org.orcid.pojo.ajaxForm.PojoUtil;
import org.orcid.pojo.ajaxForm.RedirectUri;
import org.orcid.pojo.ajaxForm.SSOCredentials;
import org.orcid.pojo.ajaxForm.Text;
import org.orcid.utils.OrcidStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller("developerToolsController")
@RequestMapping(value = { "/developer-tools" })
@PreAuthorize("!@sourceManager.isInDelegationMode() OR @sourceManager.isDelegatedByAnAdmin()")
public class DeveloperToolsController extends BaseWorkspaceController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DeveloperToolsController.class);

    private static int CLIENT_NAME_LENGTH = 255;

    @Resource
    private OrcidSSOManager orcidSSOManager;

    @Resource
    private ProfileEntityManager profileEntityManager;
    
    @Resource
    private ClientDetailsManager clientDetailsManager;
    
    @Resource
    private EmailManagerReadOnly emailManagerReadOnly;
    
    @Resource(name = "profileEntityCacheManager")
    ProfileEntityCacheManager profileEntityCacheManager;
    
    @Resource
    private ClientManager clientManager;
    
    @Resource
    private ClientManagerReadOnly clientManagerReadOnly;
    
    @RequestMapping
    public ModelAndView manageDeveloperTools() {
        ModelAndView mav = new ModelAndView("developer_tools");
        String userOrcid = getCurrentUserOrcid();
        ProfileEntity entity = profileEntityCacheManager.retrieve(userOrcid);
        mav.addObject("developerToolsEnabled", entity.getEnableDeveloperTools());
        if (!entity.getEnableDeveloperTools()) {            
            if (OrcidType.USER.equals(entity.getOrcidType())) {
                mav.addObject("error", getMessage("manage.developer_tools.user.error.enable_developer_tools"));
            } else {
                mav.addObject("error", getMessage("manage.developer_tools.user.error.invalid_user_type"));
            }
        }

        mav.addObject("hideRegistration", (sourceManager.isInDelegationMode() && !sourceManager.isDelegatedByAnAdmin()));
        mav.addObject("hasVerifiedEmail", emailManagerReadOnly.haveAnyEmailVerified(userOrcid, entity.getLastModified().getTime()));
        return mav;
    }

    @RequestMapping(value = "/client.json", method = RequestMethod.GET)
    public @ResponseBody SSOCredentials getEmptySSOCredentials(HttpServletRequest request) {
        SSOCredentials emptyObject = new SSOCredentials();
        emptyObject.setClientSecret(Text.valueOf(StringUtils.EMPTY));

        RedirectUri redirectUri = new RedirectUri();
        redirectUri.setValue(new Text());
        redirectUri.setType(Text.valueOf(RedirectUriType.DEFAULT.name()));

        Set<RedirectUri> set = new HashSet<RedirectUri>();
        set.add(redirectUri);
        emptyObject.setRedirectUris(set);
        return emptyObject;
    }
    
    @RequestMapping(value = "/get-client.json", method = RequestMethod.GET)
    public @ResponseBody Client getSSOCredentialsJson() {
        String userOrcid = getEffectiveUserOrcid();
        ProfileEntity member = profileEntityCacheManager.retrieve(userOrcid);
        Long lastModified = member.getLastModified() == null ? 0 : member.getLastModified().getTime();
        Set<org.orcid.jaxb.model.client_v2.Client> existingClients = clientManagerReadOnly.getClients(userOrcid, lastModified);

        if (existingClients.isEmpty()) {
            return null;
        }

        return Client.fromModelObject(existingClients.stream().findFirst().get());
    }
    
    @RequestMapping(value = "/create-client.json", method = RequestMethod.POST)
    public @ResponseBody Client generateSSOCredentialsJson(@RequestBody Client client) {
        validateClient(client);

        if (client.getErrors().isEmpty()) {
            org.orcid.jaxb.model.client_v2.Client clientToCreate = client.toModelObject();
            try {
                clientToCreate = clientManager.createPublicClient(clientToCreate);
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                String errorDesciption = getMessage("manage.developer_tools.group.cannot_create_client") + " " + e.getMessage();
                client.setErrors(new ArrayList<String>());
                client.getErrors().add(errorDesciption);
                return client;
            }
            client = Client.fromModelObject(clientToCreate);
        }

        return client;
    }

    @RequestMapping(value = "/update-user-credentials.json", method = RequestMethod.POST)
    public @ResponseBody Client updateUserCredentials(@RequestBody Client client) {
        validateClient(client);

        if (client.getErrors().isEmpty()) {
            org.orcid.jaxb.model.client_v2.Client clientToEdit = client.toModelObject();
            try {
                clientToEdit = clientManager.edit(clientToEdit);
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                String errorDesciption = getMessage("manage.developer_tools.group.cannot_create_client") + " " + e.getMessage();
                client.setErrors(new ArrayList<String>());
                client.getErrors().add(errorDesciption);
                return client;
            }
            client = Client.fromModelObject(clientToEdit);
        } 
        
        return client;
    }

    @RequestMapping(value = "/reset-client-secret", method = RequestMethod.POST)
    public @ResponseBody
    boolean resetClientSecret(@RequestBody String clientId) {
        //Verify this client belongs to the member
        org.orcid.jaxb.model.client_v2.Client client = clientManagerReadOnly.get(clientId);
        if(client == null) {
            return false;
        }
        
        if(!client.getGroupProfileId().equals(getCurrentUserOrcid())) {
            return false;
        }
        
        return clientManager.resetClientSecret(clientId);
    }    
    
    /**
     * Validates the Client object
     * 
     * @param ssoCredentials
     * @return true if any error is found in the ssoCredentials object
     * */
    private void validateClient(Client client) {
        client.getDisplayName().setErrors(new ArrayList<String>());
        if (PojoUtil.isEmpty(client.getDisplayName())) {
            client.getDisplayName().setErrors(Arrays.asList(getMessage("manage.developer_tools.name_not_empty")));
        } else if (client.getDisplayName().getValue().length() > CLIENT_NAME_LENGTH) {
            client.getDisplayName().setErrors(Arrays.asList(getMessage("manage.developer_tools.name_too_long")));
        } else if(OrcidStringUtils.hasHtml(client.getDisplayName().getValue())){
            client.getDisplayName().setErrors(Arrays.asList(getMessage("manage.developer_tools.name.html")));
        } 

        client.getShortDescription().setErrors(new ArrayList<String>());
        if (PojoUtil.isEmpty(client.getShortDescription())) {
            client.getShortDescription().setErrors(Arrays.asList(getMessage("manage.developer_tools.description_not_empty")));
        } else if(OrcidStringUtils.hasHtml(client.getShortDescription().getValue())) {
            client.getShortDescription().setErrors(Arrays.asList(getMessage("manage.developer_tools.description.html")));
        } 

        client.getWebsite().setErrors(new ArrayList<String>());
        if (PojoUtil.isEmpty(client.getWebsite())) {
            client.getWebsite().setErrors(Arrays.asList(getMessage("manage.developer_tools.website_not_empty")));
        } else {
            String[] schemes = { "http", "https", "ftp" };
            UrlValidator urlValidator = new UrlValidator(schemes);
            String websiteString = client.getWebsite().getValue();
            if (!urlValidator.isValid(websiteString))
                websiteString = "http://" + websiteString;

            // test validity again
            if (!urlValidator.isValid(websiteString)) {
                client.getWebsite().getErrors().add(getMessage("manage.developer_tools.invalid_website"));
            }          
        }
        
        if (client.getRedirectUris() == null){
            client.setRedirectUris(new ArrayList<RedirectUri>());            
        } 
        
        if(client.getRedirectUris().isEmpty()) {
            client.getErrors().add(getMessage("manage.developer_tools.at_least_one"));
        } else {
            for (RedirectUri rUri : client.getRedirectUris()) {
                validateRedirectUri(rUri);
                if (!rUri.getErrors().isEmpty()) {
                }
            }
        }                
    }

    /**
     * Checks if a redirect uri contains a valid URI associated to it
     * 
     * @param redirectUri
     * @return null if there are no errors, an List of strings containing error
     *         messages if any error happens
     * */
    private RedirectUri validateRedirectUri(RedirectUri redirectUri) {
        String[] schemes = { "http", "https" };
        UrlValidator urlValidator = new UrlValidator(schemes, UrlValidator.ALLOW_LOCAL_URLS);
        redirectUri.setErrors(new ArrayList<String>());
        if (!PojoUtil.isEmpty(redirectUri.getValue())) {
            try {
                String redirectUriString = redirectUri.getValue().getValue();
                if (!urlValidator.isValid(redirectUriString)) {
                    redirectUri.getErrors().add(getMessage("manage.developer_tools.invalid_redirect_uri"));
                }
            } catch (NullPointerException npe) {
                redirectUri.getErrors().add(getMessage("manage.developer_tools.empty_redirect_uri"));
            }
        } else {
            redirectUri.getErrors().add(getMessage("manage.developer_tools.empty_redirect_uri"));
        }

        return redirectUri;
    }

    /**
     * Enable developer tools on the current profile
     * 
     * @return true if the developer tools where enabled on the profile
     * */
    @RequestMapping(value = "/enable-developer-tools.json", method = RequestMethod.POST)
    public @ResponseBody
    boolean enableDeveloperTools(HttpServletRequest request) {
        return profileEntityManager.enableDeveloperTools(getCurrentUserOrcid());        
    }
}
