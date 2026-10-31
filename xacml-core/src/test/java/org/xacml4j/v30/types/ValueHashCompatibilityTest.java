package org.xacml4j.v30.types;

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
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Objects;

import com.google.common.net.InternetDomainName;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.AttributeIndex;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Content;

class ValueHashCompatibilityTest
{
	@Test
	void emptyAndContentEntitiesKeepTheirFieldArrayHash() {
		Content content = Content.fromString("<resource/>").orElseThrow();
		assertEquals(Objects.hash(AttributeIndex.empty(), null), Entity.builder().build().hashCode());
		assertEquals(Objects.hash(AttributeIndex.empty(), content),
				Entity.builder().content(content).build().hashCode());
	}

	@Test
	void wrappedValuesKeepTheirFieldArrayHash() {
		for (Value<?> value : List.of(StringVal.of("value"), IntegerVal.of(-17),
				DoubleVal.of(0.0), DoubleVal.of(-0.0), DoubleVal.of(Double.NaN), BooleanVal.of(true))) {
			assertEquals(Objects.hash(value.get(), value.getEvaluatesTo()), value.hashCode());
		}
	}

	@Test
	void namesAndDurationsKeepTheirFieldArrayHash() {
		RFC822Name name = RFC822Name.of("user@EXAMPLE.ORG");
		assertEquals(Objects.hash("user@example.org", name.getEvaluatesTo()), name.hashCode());
		for (ISO8601Duration<?> duration : List.of(ISO8601YearMonthDuration.of("P1Y2M"),
				ISO8601DayTimeDuration.of("-PT0.5S"))) {
			assertEquals(Objects.hash(duration.duration(), duration.getEvaluatesTo()), duration.hashCode());
		}
		DNSName dns = DNSName.of("example.org", PortRange.getRange(80, 90));
		assertEquals(Objects.hash(InternetDomainName.from("example.org"), dns.getPortRange()), dns.hashCode());
	}

	@Test
	void addressesPortsAndPathsKeepNullFieldsInTheirHash() {
		assertEquals(Objects.hash(null, null), PortRange.getAnyPort().hashCode());
		assertEquals(Objects.hash(80, null), PortRange.getRangeFrom(80).hashCode());
		assertEquals(Objects.hash(null, 90), PortRange.getRangeUntil(90).hashCode());
		assertEquals(Objects.hash(80, 90), PortRange.getRange(80, 90).hashCode());
		for (IPAddress address : List.of(IPAddress.of("127.0.0.1"), IPAddress.of("127.0.0.1/255.255.255.0:80-90"))) {
			assertEquals(Objects.hash(address.getAddress(), address.getMask(), address.getRange()), address.hashCode());
		}
		Path xpath = Path.of("/resource", Content.PathType.XPATH, CategoryId.RESOURCE);
		Path jpath = Path.of("$.resource", Content.PathType.JPATH, null);
		assertEquals(Objects.hash(CategoryId.RESOURCE, "/resource", Content.PathType.XPATH), xpath.hashCode());
		assertEquals(Objects.hash(null, "$.resource", Content.PathType.JPATH), jpath.hashCode());
	}
}
