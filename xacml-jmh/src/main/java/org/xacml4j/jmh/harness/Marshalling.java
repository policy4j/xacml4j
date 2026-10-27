package org.xacml4j.jmh.harness;

/*
 * #%L
 * Xacml4J JMH Benchmarks
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

import java.util.Map;

import org.xacml4j.v30.marshal.MarshallingProvider;
import org.xacml4j.v30.marshal.MediaType;
import org.xacml4j.v30.marshal.PolicyUnmarshaller;
import org.xacml4j.v30.marshal.RequestMarshaller;
import org.xacml4j.v30.marshal.RequestUnmarshaller;
import org.xacml4j.v30.marshal.ResponseUnmarshaller;

/** XACML unmarshallers and request marshallers by media type, from providers discovered once. */
public final class Marshalling
{
	private static final Map<MediaType, MarshallingProvider> PROVIDERS = MarshallingProvider.discoverProviders();

	private Marshalling(){
	}

	public static PolicyUnmarshaller policies(MediaType mediaType){
		return provider(mediaType).newPolicyUnmarshaller()
				.orElseThrow(() -> new IllegalArgumentException("No policy unmarshaller for " + mediaType));
	}

	public static RequestUnmarshaller requests(MediaType mediaType){
		return provider(mediaType).newRequestUnmarshaller()
				.orElseThrow(() -> new IllegalArgumentException("No request unmarshaller for " + mediaType));
	}

	/** @return a request marshaller, used to write corpus requests for the parse benchmarks */
	public static RequestMarshaller requestMarshaller(MediaType mediaType){
		return provider(mediaType).newRequestMarshaller()
				.orElseThrow(() -> new IllegalArgumentException("No request marshaller for " + mediaType));
	}

	public static ResponseUnmarshaller responses(MediaType mediaType){
		return provider(mediaType).newResponseUnmarshaller()
				.orElseThrow(() -> new IllegalArgumentException("No response unmarshaller for " + mediaType));
	}

	private static MarshallingProvider provider(MediaType mediaType){
		MarshallingProvider provider = PROVIDERS.get(mediaType);
		if(provider == null){
			throw new IllegalArgumentException("No marshalling provider for " + mediaType + "; known: " + PROVIDERS.keySet());
		}
		return provider;
	}
}
