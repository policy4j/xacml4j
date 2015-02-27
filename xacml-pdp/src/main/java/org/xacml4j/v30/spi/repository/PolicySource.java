package org.xacml4j.v30.spi.repository;

/*
 * #%L
 * Xacml4J PDP related classes
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
import org.xacml4j.v30.pdp.Policy;
import org.xacml4j.v30.pdp.PolicySet;

import java.io.Closeable;
import java.io.IOException;

/**
 * Represents policy source abstraction
 *
 * @author Giedrius Trumpickas
 */
public interface PolicySource extends Closeable
{
    /**
     * Gets policy source identifier
     *
     * @return policy source identifier
     */
    String getId();

    /**
     * Creates {@link org.xacml4j.v30.spi.repository.PolicyReferenceResolver}
     * for this policy source
     *
     * @return {@link org.xacml4j.v30.spi.repository.PolicyReferenceResolver}
     */
    PolicyReferenceResolver createResolver();

    /**
     * Gets all available policies at this source.
     * Usually call to this method is resource intensive due
     * the fact that method returns an {@link java.lang.Iterable}
     * to all policies at the source
     *
     * @return an {@link java.lang.Iterable} over all
     * available {@link org.xacml4j.v30.CompositeDecisionRule}
     */
    Iterable<CompositeDecisionRule> getPolicies() throws IOException;

    /**
     * Adds change listener
     *
     * @param l a change listener
     */
    void addListener(ChangeListener l);

    /**
     * Removes change listener
     *
     * @param l a change listener
     */
    void removeListener(ChangeListener l);

    /**
     * A policy source change listener
     */
    public interface ChangeListener
    {
        void policyAdded(Policy ... p);
        void policyRemoved(Policy ... p);
        void policySetAdded(PolicySet ... p);
        void policySetRemoved(PolicySet ... p);
    }
}
