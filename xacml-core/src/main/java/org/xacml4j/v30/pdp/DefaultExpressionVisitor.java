package org.xacml4j.v30.pdp;

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

import org.xacml4j.v30.AttributeExp.AttributeExpVisitor;
import org.xacml4j.v30.BagOfAttributeExp.BagOfAttributeVisitor;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.pdp.AttributeDesignator.AttributeDesignatorVisitor;
import org.xacml4j.v30.pdp.AttributeSelector.AttributeSelectorVisitor;
import org.xacml4j.v30.pdp.FunctionReference.FunctionReferenceVisitor;
import org.xacml4j.v30.pdp.VariableReference.VariableReferenceVisitor;


/**
 * A default interface for {@link Expression} visitors
 *
 * @author Giedrius Trumpickas
 */
public interface DefaultExpressionVisitor extends
                                          Apply.ApplyVisitor, BagOfAttributeVisitor, AttributeExpVisitor, AttributeSelectorVisitor,
AttributeDesignatorVisitor, VariableReferenceVisitor, FunctionReferenceVisitor
{

}
