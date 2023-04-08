package org.xacml4j.v30.marshal;

/*
 * #%L
 * Xacml4J Core Engine Implementation
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

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;

public interface MediaType
{
	boolean is(String mediaType);
	boolean is(MediaType mediaType);
	static Optional<MediaType> of(String mediaType){
		return Arrays.asList(Type.values())
		             .stream()
		             .filter(t->t.is(mediaType))
		             .map(MediaType.class::cast)
		             .findFirst();
	}

	enum Type implements MediaType
	{


		XACML20_XML(com.google.common.net.MediaType.create("application", "xacml+xml")
		                                           .withParameter("version","2.0")
		                                           .withCharset(Charsets.UTF_8)),

		XACML30_XML(com.google.common.net.MediaType.create("application", "xacml+xml")
		                                           .withParameter("version","3.0")
		                                           .withCharset(Charsets.UTF_8)),

		XACML30_JSON(com.google.common.net.MediaType.create("application", "xacml+json")
		                                            .withParameter("version","3.0")
		                                            .withCharset(Charsets.UTF_8)),

		XACML30_PROTO(com.google.common.net.MediaType.create("application", "xacml+protobuf")
		                                             .withParameter("version","3.0"),
		              com.google.common.net.MediaType.create("application","xacml+x-protobuf")
		                                             .withParameter("version","3.0"),
		              com.google.common.net.MediaType.create("application","xacml+x-protobuf")
		                                             .withParameter("version","3.0"));

		private Set<com.google.common.net.MediaType> mediaTypes;

		Type(com.google.common.net.MediaType... mediaTypes) {
			this.mediaTypes = ImmutableSet.<com.google.common.net.MediaType>builder()
			                              .add(mediaTypes)
			                              .build();
		}

		public boolean is(String mediaType) {
			Optional<com.google.common.net.MediaType> mt = parse(mediaType);
			return mediaTypes.stream()
			                 .filter(v -> mt.map(t -> t.is(v))
			                                .orElse(false))
			                 .findFirst().isPresent();
		}

		public boolean is(MediaType mediaType) {
			return is(mediaType.toString());
		}

		private static Optional<com.google.common.net.MediaType> parse(String type) {
			try {
				return Optional.of(com.google.common.net.MediaType.parse(type));
			} catch (IllegalArgumentException e) {
				return Optional.empty();
			}
		}
	}
}
