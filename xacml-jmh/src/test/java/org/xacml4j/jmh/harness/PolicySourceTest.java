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

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.marshal.MediaType;

/** A policy document held in memory, equal by its bytes (design D4). */
public class PolicySourceTest
{
	private static final MediaType XACML30 = MediaType.Type.XACML30_XML;
	private static final MediaType XACML20 = MediaType.Type.XACML20_XML;

	@Test
	public void sourcesWithTheSameNameTypeAndBytesAreEqual()
	{
		PolicySource a = new PolicySource("a.xml", XACML30, "<Policy/>".getBytes(UTF_8));
		PolicySource b = new PolicySource("a.xml", XACML30, "<Policy/>".getBytes(UTF_8));

		assertThat(a).isEqualTo(b);
		assertThat(a.hashCode()).isEqualTo(b.hashCode());
	}

	@Test
	public void sourcesDifferWhenTheNameTheTypeOrTheBytesDiffer()
	{
		PolicySource a = new PolicySource("a.xml", XACML30, "<Policy/>".getBytes(UTF_8));

		assertThat(a).isNotEqualTo(new PolicySource("b.xml", XACML30, "<Policy/>".getBytes(UTF_8)));
		assertThat(a).isNotEqualTo(new PolicySource("a.xml", XACML20, "<Policy/>".getBytes(UTF_8)));
		assertThat(a).isNotEqualTo(new PolicySource("a.xml", XACML30, "<PolicySet/>".getBytes(UTF_8)));
	}

	@Test
	public void sourceKeepsItsOwnCopyOfTheBytes()
	{
		byte[] bytes = "<Policy/>".getBytes(UTF_8);
		PolicySource source = new PolicySource("a.xml", XACML30, bytes);

		bytes[0] = 'X';
		source.content()[0] = 'Y';

		assertThat(source.content()).isEqualTo("<Policy/>".getBytes(UTF_8));
	}

	@Test
	public void everyStreamReadsAllBytes() throws Exception
	{
		PolicySource source = new PolicySource("a.xml", XACML30, "<Policy/>".getBytes(UTF_8));

		try(InputStream first = source.open(); InputStream second = source.open()){
			assertThat(first.readAllBytes()).isEqualTo("<Policy/>".getBytes(UTF_8));
			assertThat(second.readAllBytes()).isEqualTo("<Policy/>".getBytes(UTF_8));
		}
	}
}
