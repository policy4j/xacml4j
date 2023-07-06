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

import java.util.Optional;

import org.xacml4j.v30.marshal.MarshallingProvider;
import org.xacml4j.v30.marshal.MediaType;
import org.xacml4j.v30.marshal.PolicyMarshaller;
import org.xacml4j.v30.marshal.PolicyUnmarshaller;
import org.xacml4j.v30.marshal.RequestMarshaller;
import org.xacml4j.v30.marshal.RequestUnmarshaller;
import org.xacml4j.v30.marshal.ResponseMarshaller;
import org.xacml4j.v30.marshal.ResponseUnmarshaller;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;

import com.google.auto.service.AutoService;

@AutoService(MarshallingProvider.class)
public class Xacm30MarshallingProvider implements MarshallingProvider
{
	@Override
	public MediaType getMediaType() {
		return MediaType.Type.XACML30_XML;
	}

	@Override
	public Optional<PolicyMarshaller> newPolicyMarshaller() {
		return Optional.of(new XacmlPolicyMarshaller());
	}

	@Override
	public Optional<PolicyUnmarshaller> newPolicyUnmarshaller(FunctionProvider functionProvider, DecisionCombiningAlgorithmProvider algorithmProvider) {
		return Optional.of(new XacmlPolicyUnmarshaller(functionProvider, algorithmProvider, false));
	}

	@Override
	public Optional<RequestMarshaller> newRequestMarshaller() {
		return Optional.of(new Xacml30RequestContextMarshaller());
	}

	@Override
	public Optional<RequestUnmarshaller> newRequestUnmarshaller() {
		return Optional.of(new Xacml30RequestContextUnmarshaller());
	}

	@Override
	public Optional<ResponseMarshaller> newResponseMarshaller() {
		return Optional.of(new Xacml30ResponseContextMarshaller());
	}

	@Override
	public Optional<ResponseUnmarshaller> newResponseUnmarshaller() {
		return Optional.of(new Xacml30ResponseContextUnmarshaller());
	}
}
