package org.xacml4j.v30.xml;

/*
 * #%L
 * Xacml XML representation marshallers
 * %%
 * Copyright (C) 2009 - 2023 Xacml4J.org
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

import javax.xml.bind.JAXBElement;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.marshal.PolicyUnmarshaller;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;

public final class Xacml30PolicyUnmarshaller extends BaseJAXBUnmarshaller<CompositeDecisionRule>
		implements PolicyUnmarshaller
{
	private final Xacml30PolicyFromJaxbToObjectModelMapper mapper;

	public Xacml30PolicyUnmarshaller(FunctionProvider functionProvider,
	                                 DecisionCombiningAlgorithmProvider combiningAlgorithmProvider)
			throws Exception
	{
		super(JAXBUtils.getInstance());
		this.mapper = new Xacml30PolicyFromJaxbToObjectModelMapper(functionProvider,
		                                                           combiningAlgorithmProvider);
	}

	public Xacml30PolicyUnmarshaller()
			throws Exception
	{
		this(FunctionProvider.builder()
		                     .withDefaultFunctions()
		                     .build(),
		      DecisionCombiningAlgorithmProvider.builder()
		                                        .withDefaultAlgorithms()
		                                        .build());
	}

	@Override
	protected CompositeDecisionRule create(JAXBElement jaxbInstance) throws SyntaxException {
		return mapper.create(jaxbInstance);
	}
}
