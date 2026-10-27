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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

import org.xacml4j.v30.marshal.MediaType;

/**
 * One policy document of a corpus, held in memory. A class rather than a
 * record, so that equality compares the bytes.
 */
public final class PolicySource
{
	private final String name;
	private final MediaType mediaType;
	private final byte[] content;

	/**
	 * @param name the file name of the document, unique within its corpus
	 * @param mediaType the XACML media type of the document
	 * @param content the document bytes; copied
	 */
	public PolicySource(String name, MediaType mediaType, byte[] content){
		this.name = Objects.requireNonNull(name, "name");
		this.mediaType = Objects.requireNonNull(mediaType, "mediaType");
		this.content = Objects.requireNonNull(content, "content").clone();
	}

	public String name(){
		return name;
	}

	public MediaType mediaType(){
		return mediaType;
	}

	/** @return a copy of the document bytes */
	public byte[] content(){
		return content.clone();
	}

	/** @return a new stream on the held bytes, without a copy */
	public InputStream open(){
		return new ByteArrayInputStream(content);
	}

	@Override
	public boolean equals(Object o){
		return o instanceof PolicySource other
				&& name.equals(other.name)
				&& mediaType.equals(other.mediaType)
				&& Arrays.equals(content, other.content);
	}

	@Override
	public int hashCode(){
		return Objects.hash(name, mediaType) * 31 + Arrays.hashCode(content);
	}

	@Override
	public String toString(){
		return "PolicySource{" + name + ", " + mediaType + ", " + content.length + " bytes}";
	}
}
