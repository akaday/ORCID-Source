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
package org.orcid.core.manager;

import org.orcid.jaxb.model.client_v2.Client;
import org.orcid.pojo.ajaxForm.Member;

public interface MembersManager {

    Member createMember(Member member) throws IllegalArgumentException;
    
    Member updateMemeber(Member member) throws IllegalArgumentException;
    
    Member getMember(String memberId);      
    
    Client getClient(String clientId);
    
    Client updateClient(Client client);

}
