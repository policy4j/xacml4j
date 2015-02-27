package org.xacml4j.v30.spi.repository;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
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

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.pdp.*;


/**
 * A XACML {@link org.xacml4j.v30.pdp.PolicyReference} or
 * {@link org.xacml4j.v30.pdp.PolicySetReference} resolution capability
 *
 * @author Giedrius Trumpickas
 */
public interface PolicyReferenceResolver
{
    /**
     * Resolves a composite decision rule via
     * identifier and version match constraints
     * @param id a composite decision rule identifier
     * @param version a policy version match constraint
     * @param earliest an earliest acceptable policy version match constraint
     * @param latest an latest acceptable policy version match constraint
     */
    CompositeDecisionRule resolve(String id, String version,
                                  String earliest, String latest);

    /**
     * Resolves a composite decision rule via
     * identifier and version match constraints
     * @param id a composite decision rule identifier
     * @param version a policy version match constraint
     * @throws org.xacml4j.v30.pdp.PolicyResolutionException
     */
    CompositeDecisionRule resolve(String id, String version)
            throws PolicyResolutionException;

	/**
	 * Resolves a given {@link org.xacml4j.v30.pdp.PolicyReference}
	 *
	 * @param ref a policy references
	 * @return {@link Policy} instance
	 * @throws olicyResolutionException
	 */
	Policy resolve(PolicyReference ref)
		throws PolicyResolutionException;

	/**
	 * Resolves a given {@link org.xacml4j.v30.pdp.PolicySetReference}
	 *
	 *
	 * @param ref a policy references
	 * @return {@link PolicySet} instance
	 * @throws PolicyResolutionException
	 */
	PolicySet resolve(PolicySetReference ref)
		throws PolicyResolutionException;
}
