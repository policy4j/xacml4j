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

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.pdp.*;

/**
 * A default implementation of {@link PolicyReferenceResolver}
 *
 * @author Giedrius Trumpickas
 */

final class DefaultPolicyReferenceResolver
	implements PolicyReferenceResolver
{
	private final static Logger log = LoggerFactory.getLogger(DefaultPolicyReferenceResolver.class);

    /**
     * A high performance non-blocking
     * policy index, for quick policy
     * lookup by id and version
     */
    private PolicyIndex ruleIndex;

	private DefaultPolicyReferenceResolver(Builder b){
        this.ruleIndex = b.source;
	}

    @Override
    public CompositeDecisionRule resolve(String id, String version){
        return resolve(id, version ,null, null);
    }

    @Override
    public CompositeDecisionRule resolve(String id, String version,
                                         String earliest, String latest)
            throws PolicyResolutionException
    {
       PolicySet p =  resolve(PolicySetReference
               .builder(id)
               .version(version)
               .earliest(earliest)
               .latest(latest)
               .build());
        if(p != null){
            return p;
        }
       return resolve(PolicyReference
               .builder(id)
               .version(version)
               .earliest(earliest)
               .latest(latest)
               .build());
    }

    @Override
	public Policy resolve(PolicyReference ref)
			throws PolicyResolutionException
	{
        Iterable<Policy> found = ruleIndex.getPolicies(ref);
        Policy p = Iterables.getFirst(found, null);
        if(p == null) {
            return null;
        }
        if(log.isDebugEnabled()) {
            log.debug("Resolved policy id=\"{}\" " +
                            "version=\"{}\" for references=\"{}\" " +
                            "from repository=\"{}\"",
                    new Object[]{p.getId(), p.getVersion(),
                            ref, ruleIndex.getId()});
        }
        return p;
	}

    @Override
    public PolicySet resolve(PolicySetReference ref)
            throws PolicyResolutionException
    {
        Iterable<PolicySet> found = ruleIndex.getPolicySets(ref);
        PolicySet p = Iterables.getFirst(found, null);
        if(p == null) {
            return null;
        }
        if(log.isDebugEnabled()) {
            log.debug("Resolved policy set id=\"{}\" " +
                                    "version=\"{}\" for references=\"{}\" " +
                                    "from repository=\"{}\"",
                           new Object[]{p.getId(), p.getVersion(),
                                   ref, ruleIndex.getId()});
        }
        return p;
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder
    {
        private PolicyIndex source;

        public Builder source(PolicySource source){
            Preconditions.checkNotNull(source);
            this.source = new PolicyIndex(source);
            return this;
        }

        public PolicyReferenceResolver build(){
            return new DefaultPolicyReferenceResolver(this);
        }
    }
}
