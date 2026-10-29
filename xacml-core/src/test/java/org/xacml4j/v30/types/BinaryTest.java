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
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import com.google.common.truth.Truth8;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.List;

import org.junit.jupiter.api.Test;

public class BinaryTest
{
	@Test
	public void binaryValuesKeepTheBytesOwnedAtConstruction() throws Exception
	{
		byte[] bytes = {0, 1, 3, 5};
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		ByteBuffer direct = ByteBuffer.allocateDirect(bytes.length).put(bytes).flip().limit(3).position(1);
		List<Value<Binary>> values = List.of(HexBinary.of(buffer), HexBinary.ofAny(buffer),
				Base64Binary.ofAny(buffer), HexBinary.of(bytes), Base64Binary.ofAny(bytes),
				HexBinary.of(buffer.asReadOnlyBuffer()), Base64Binary.ofAny(buffer.asReadOnlyBuffer()),
				HexBinary.of(direct), Base64Binary.ofAny(direct));
		Binary expected = Binary.of(new byte[]{0, 1, 3, 5});

		buffer.put(0, (byte)9);
		direct.put(0, (byte)9);
		bytes[1] = 8;
		for(Value<Binary> value : values){
			assertEquals(expected, value.get());
			assertEquals(expected.hashCode(), value.get().hashCode());
			assertEquals("00010305", value.get().toHexEncoded());
			assertEquals("AAEDBQ==", value.get().toBase64Encoded());
			assertThrows(ReadOnlyBufferException.class, () -> value.get().asByteBuffer().put((byte)7));
			ByteArrayOutputStream written = new ByteArrayOutputStream();
			value.get().writeTo(written);
			assertArrayEquals(new byte[]{0, 1, 3, 5}, written.toByteArray());
			ByteArrayOutputStream serialized = new ByteArrayOutputStream();
			try(ObjectOutputStream output = new ObjectOutputStream(serialized)){
				value.get().writeExternal(output);
			}
			try(ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(serialized.toByteArray()))){
				byte[] restored = new byte[4];
				input.readFully(restored);
				assertArrayEquals(new byte[]{0, 1, 3, 5}, restored);
			}
		}
	}

	@Test
	public void aBufferSliceKeepsItsViewAndWholeArrayEncoding()
	{
		byte[] bytes = {0, 1, 3, 5};
		ByteBuffer slice = ByteBuffer.wrap(bytes).position(1).limit(3).slice();
		slice.mark().position(1);
		Binary value = Binary.of(slice);
		bytes[0] = 9;
		slice.put(1, (byte)8);

		assertEquals("00010305", value.toHexEncoded());
		ByteBuffer view = value.asByteBuffer();
		assertEquals(2, view.capacity());
		assertEquals(2, view.limit());
		assertEquals(1, view.position());
		assertEquals(3, view.get());
		assertEquals(0, view.reset().position());
		assertEquals(1, view.get());
	}

	@Test
	public void readingExternalDataCannotReplaceAnExistingValue() throws Exception
	{
		Binary value = Binary.of(new byte[]{1, 2});
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try(ObjectOutputStream output = new ObjectOutputStream(bytes)){
			Binary.of(new byte[]{3, 4}).writeExternal(output);
		}
		try(ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){
			assertThrows(InvalidObjectException.class, () -> value.readExternal(input));
		}
		assertEquals("0102", value.toHexEncoded());
	}

	@Test
	public void testCreateFromBinaryAndEncodeToBase64()
	{
		byte[] v0 = {0, 1, 3, 5};
		byte[] v1 = {0, 1, 3, 5};
		Binary value1 = Binary.of(v0);
		Binary value2 = Binary.of(v1);
		assertEquals(value1, value2);
		assertEquals("AAEDBQ==", value1.toBase64Encoded());
	}

	@Test
	public void testCreateFromZeroLenghtArray()
	{
		byte[] v = {};
		Binary bv = Binary.of(v);
		assertEquals("", bv.toBase64Encoded());
		assertEquals("", bv.toHexEncoded());
	}

	@Test
	public void testCreateFromNull()
	{
		Truth8.assertThat(Binary.ofAnyBinary(null)).isEmpty();
	}

	@Test
	public void testCreateFromBinaryAndEncodeToHex()
	{
		byte[] v0 = {0, 1, 3, 5};
		byte[] v1 = {0, 1, 3, 5};
		Binary value1 = Binary.of(v0);
		Binary value2 = Binary.of(v1);
		assertEquals(value1, value2);
		assertEquals("00010305", value1.toHexEncoded());
	}
}
