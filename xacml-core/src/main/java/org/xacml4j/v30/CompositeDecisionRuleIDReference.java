package org.xacml4j.v30;

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


/**
 * An interface for {@link org.xacml4j.v30.CompositeDecisionRule}
 * references
 *
 * @author Giedrius Trumpickas
 */
public interface CompositeDecisionRuleIDReference
	extends DecisionRule
{
    /**
     * Gets version matching constraint
     *
     * @return {@link org.xacml4j.v30.VersionMatch} or <code>null</code>
     */
	VersionMatch getVersion();

    /**
     * Gets earliest version matching constraint
     *
     * @return {@link org.xacml4j.v30.VersionMatch}
     */
	VersionMatch getEarliestVersion();

    /**
     * Gets latest version matching constraint
     *
     * @return {@link org.xacml4j.v30.VersionMatch}
     */
	VersionMatch getLatestVersion();

    /**
     * Tests if this reference is a reference to the
     * given decision rule
     *
     * @param r a decision rule
     * @return <code>true</code> if this reference
     * is a reference to the given decision rule
     */
	boolean isReferenceTo(CompositeDecisionRule r);
}
