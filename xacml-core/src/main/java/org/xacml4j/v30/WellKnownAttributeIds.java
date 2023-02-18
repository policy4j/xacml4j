package org.xacml4j.v30;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2019 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */


import java.util.Map;
import java.util.Optional;

import org.xacml4j.v30.types.Entity;

/**
 * XACML attribute identifier
 *
 * @author Giedrius Trumpickas
 */
public class WellKnownAttributeIds extends SemanticalIdentifier
{
    public final static WellKnownAttributeIds RESOURCE_ID = new WellKnownAttributeIds("urn:oasis:names:tc:xacml:1.0:resource:resource-id", "resource-id");
    public final static WellKnownAttributeIds MDP_MULTIPLE_SELECTOR = new WellKnownAttributeIds("urn:oasis:names:tc:xacml:3.0:profile:multiple:content-selector", "multiple:content-selector");
    public final static WellKnownAttributeIds MDP_SCOPE = new WellKnownAttributeIds("urn:oasis:names:tc:xacml:3.0:profile:multiple:scope", "scope");
    public final static WellKnownAttributeIds CONTENT_SELECTOR = new WellKnownAttributeIds("urn:oasis:names:tc:xacml:3.0:content-selector", "content-selector");

    /**
     * This identifier indicates the name of the subject.
     */
    public final static WellKnownAttributeIds SUBJECT_ID = new WellKnownAttributeIds("urn:oasis:names:tc:xacml:1.0:subject:subject-id", "subject-id");

    /**
     * This identifier indicates the security domain of the subject.
     * It identifies the administrator and policy that manages the
     * name-space in which the subject id is administered.
     */
    public final static WellKnownAttributeIds SUBJECT_ID_QUALIFIER = new WellKnownAttributeIds("urn:oasis:names:tc:xacml:1.0:subject:subject-id-qualifier", "subject-id-qualifier");

    /**
     * This identifier indicates a public key used to confirm the subject's identity.
     */
    public final static WellKnownAttributeIds KEY_INFO = new WellKnownAttributeIds("urn:oasis:names:tc:xacml:1.0:subject:key-info", "key-info");

    /**
     * This identifier indicates the time at which the subject was authenticated.
     */
    public final static WellKnownAttributeIds AUTHN_TIME = new WellKnownAttributeIds("urn:oasis:names:tc:xacml:1.0:subject:authentication-time", "authentication-time");

    /**
     * This identifier indicates the method used to authenticate the subject.
     */
    public final static WellKnownAttributeIds AUTHN_METHOD = new WellKnownAttributeIds("urn:oasis:names:tc:xacml:1.0:subject:authentication-method", "authentication-time");

    /**
     * This identifier indicates the time at which the subject initiated the
     * access request, according to the PEP.
     */
    public final static WellKnownAttributeIds REQUEST_TIME = new WellKnownAttributeIds("urn:oasis:names:tc:xacml:1.0:subject:request-time", "request-time");

    /**
     * This identifier indicates the time at which the subject's
     * current session began, according to the PEP.
     */
    public final static WellKnownAttributeIds SESSION_START_TIME = new WellKnownAttributeIds("urn:oasis:names:tc:xacml:1.0:subject:session-start-time", "session-start-time");

    /**
     * This identifier indicates an identifier for subject's
     * current session.
     */
    public final static WellKnownAttributeIds SESSION_IDENTIFIER = new WellKnownAttributeIds("urn:xacml4j:names:tc:xacml:1.0:subject:session-id", "session-id");

    /**
     * The following identifiers indicate the location where authentication
     * credentials were activated. This identifier indicates that
     * the location is expressed as an IP address.
     */
    public final static WellKnownAttributeIds AUTHN_LOCALITY_IP_ADDRESS = new WellKnownAttributeIds("urn:oasis:names:tc:xacml:1.0:subject:authn-locality:ip-address", "ip-address");

    /**
     * This identifier indicates that the location is expressed as a DNS name.
     */
    public final static WellKnownAttributeIds AUTHN_LOCALITY_DNS_NAME = new WellKnownAttributeIds("urn:oasis:names:tc:xacml:1.0:subject:authn-locality:dns-name", "dns-name");

    public final static WellKnownAttributeIds USER_PASSWORD = new WellKnownAttributeIds("http://www.ietf.org/rfc/rfc2256.txt#userPassword", "userPassword");

    /**
     * This attribute identifies the resource to which access is requested.
     */
    public final static WellKnownAttributeIds RESOURCE = new WellKnownAttributeIds("urn:oasis:names:tc:xacml:1.0:resource:resource-id", "resource-id");

    /**
     * This attribute identifies the namespace of the top element(s) of the
     * contents of the {@link Entity#getContent()}.
     * In the case where the resource content is supplied in
     * the request context and the resource namespaces are defined in the resource,
     * the PEP MAY provide this attribute in the request to
     * indicate the namespaces of the resource content. In this case
     * there SHALL be one value of this attribute for each unique namespace
     * of the top level elements in the {@link Entity#getContent()}.
     * The type of the corresponding attribute SHALL be {@link org.xacml4j.v30.types.XacmlTypes#ANYURI}
     */
    public final static WellKnownAttributeIds TARGET_NAMESPACE = new WellKnownAttributeIds("urn:oasis:names:tc:xacml:2.0:resource:target-namespace", "target-namespace");

    private final static Map<String, WellKnownAttributeIds> By_ID = SemanticalIdentifier.getById(WellKnownAttributeIds.class);
    private final static Map<String, WellKnownAttributeIds> By_ABBREVIATED_ID = SemanticalIdentifier.getByAbbrId(WellKnownAttributeIds.class);

    private final static boolean ALWAYS_USE_SUPPLIER = Optional.ofNullable(System
            .getProperty(WellKnownAttributeIds.class.getName() + ".ALWAYS_USE_SUPPLIER"))
            .map(v->Boolean.parseBoolean(v)).orElse(false);

    private WellKnownAttributeIds(String id, String abbreviatedId) {
        super(id, abbreviatedId);
    }
    private WellKnownAttributeIds(String id) {
        super(id, id);
    }

    public final static Optional<WellKnownAttributeIds> of(String id) {
        return of(id, By_ID, By_ABBREVIATED_ID,
                ()->Optional.of(new WellKnownAttributeIds(id)),
                ALWAYS_USE_SUPPLIER);
    }
}

