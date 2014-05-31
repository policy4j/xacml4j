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

/*
 * JUG Java Uuid Generator
 *
 * Copyright (c) 2002 Tatu Saloranta, tatu.saloranta@iki.fi
 *
 * Licensed under the License specified in the file LICENSE which is
 * included with the source code.
 * You may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.Serializable;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Random;

/**
 * EthernetAddress encapsulates the 6-byte MAC address defined in IEEE 802.1
 * standard.
 */
public class EUI48 implements Serializable, Cloneable,
		Comparable<EUI48>
{

	private static final long serialVersionUID = -4797835241153743083L;

	private final static char[] HEX_CHARS = "0123456789abcdefABCDEF"
			.toCharArray();

	/**
	 * We may need a random number generator, for creating dummy ethernet
	 * address if no real interface is found.
	 */
	protected static Random _rnd;

	/**
	 * 48-bit MAC address, stored in 6 lowest-significant bytes (in big endian
	 * notation)
	 */
	protected final long _address;

	/**
	 * Lazily-constructed String serialization
	 */
	private volatile String _asString;

	/*
	 * /**********************************************************************
	 * /* Instance construction
	 * /**********************************************************************
	 */

	/**
	 * String constructor; given a 'standard' ethernet MAC address string (like
	 * '00:C0:F0:3D:5B:7C'), constructs an EthernetAddress instance.
	 *
	 * Note that string is case-insensitive, and also that leading zeroes may be
	 * omitted. Thus '00:C0:F0:3D:5B:7C' and '0:c0:f0:3d:5b:7c' are equivalent,
	 * and a 'null' address could be passed as ':::::' as well as
	 * '00:00:00:00:00:00' (or any other intermediate combination).
	 *
	 * @param addrStr
	 *            String representation of the ethernet address
	 */
	public EUI48(String addrStr) throws NumberFormatException {
		int len = addrStr.length();
		long addr = 0L;

		/*
		 * Ugh. Although the most logical format would be the 17-char one (12
		 * hex digits separated by colons), apparently leading zeroes can be
		 * omitted. Thus... Can't just check string length. :-/
		 */
		for (int i = 0, j = 0; j < 6; ++j) {
			if (i >= len) {
				// Is valid if this would have been the last byte:
				if (j == 5) {
					addr <<= 8;
					break;
				}
				throw new NumberFormatException(
						"Incomplete ethernet address (missing one or more digits");
			}

			char c = addrStr.charAt(i);
			++i;
			int value;

			// The whole number may be omitted (if it was zero):
			if (c == ':') {
				value = 0;
			} else {
				// No, got at least one digit?
				if (c >= '0' && c <= '9') {
					value = (c - '0');
				} else if (c >= 'a' && c <= 'f') {
					value = (c - 'a' + 10);
				} else if (c >= 'A' && c <= 'F') {
					value = (c - 'A' + 10);
				} else {
					throw new NumberFormatException("Non-hex character '" + c
							+ "'");
				}

				// How about another one?
				if (i < len) {
					c = addrStr.charAt(i);
					++i;
					if (c != ':') {
						value = (value << 4);
						if (c >= '0' && c <= '9') {
							value |= (c - '0');
						} else if (c >= 'a' && c <= 'f') {
							value |= (c - 'a' + 10);
						} else if (c >= 'A' && c <= 'F') {
							value |= (c - 'A' + 10);
						} else {
							throw new NumberFormatException(
									"Non-hex character '" + c + "'");
						}
					}
				}
			}

			addr = (addr << 8) | value;

			if (c != ':') {
				if (i < len) {
					if (addrStr.charAt(i) != ':') {
						throw new NumberFormatException("Expected ':', got ('"
								+ addrStr.charAt(i) + "')");
					}
					++i;
				} else if (j < 5) {
					throw new NumberFormatException(
							"Incomplete ethernet address (missing one or more digits");
				}
			}
		}
		_address = addr;
	}

	/**
	 * Binary constructor that constructs an instance given the 6 byte (48-bit)
	 * address. Useful if an address is saved in binary format (for saving space
	 * for example).
	 */
	public EUI48(byte[] addr) throws NumberFormatException {
		if (addr.length != 6) {
			throw new NumberFormatException(
					"Ethernet address has to consist of 6 bytes");
		}
		long l = addr[0] & 0xFF;
		for (int i = 1; i < 6; ++i) {
			l = (l << 8) | (addr[i] & 0xFF);
		}
		_address = l;
	}

	/**
	 * Another binary constructor; constructs an instance from the given long
	 * argument; the lowest 6 bytes contain the address.
	 *
	 * @param addr
	 *            long that contains the MAC address in 6 least significant
	 *            bytes.
	 */
	public EUI48(long addr) {
		_address = addr;
	}

	/**
	 * Default cloning behaviour (bitwise copy) is just fine...
	 */
	@Override
	public Object clone() {
		return new EUI48(_address);
	}

	/**
	 * Constructs a new EthernetAddress given the byte array that contains
	 * binary representation of the address.
	 *
	 * Note that calling this method returns the same result as would using the
	 * matching constructor.
	 *
	 * @param addr
	 *            Binary representation of the ethernet address
	 *
	 * @throws NumberFormatException
	 *             if addr is invalid (less or more than 6 bytes in array)
	 */
	public static EUI48 valueOf(byte[] addr)
			throws NumberFormatException {
		return new EUI48(addr);
	}

	/**
	 * Constructs a new EthernetAddress given the byte array that contains
	 * binary representation of the address.
	 *
	 * Note that calling this method returns the same result as would using the
	 * matching constructor.
	 *
	 * @param addr
	 *            Binary representation of the ethernet address
	 *
	 * @throws NumberFormatException
	 *             if addr is invalid (less or more than 6 ints in array)
	 */
	public static EUI48 valueOf(int[] addr)
			throws NumberFormatException {
		byte[] bAddr = new byte[addr.length];

		for (int i = 0; i < addr.length; ++i) {
			bAddr[i] = (byte) addr[i];
		}
		return new EUI48(bAddr);
	}

	/**
	 * Constructs a new EthernetAddress given a string representation of the
	 * ethernet address.
	 *
	 * Note that calling this method returns the same result as would using the
	 * matching constructor.
	 *
	 * @param addrStr
	 *            String representation of the ethernet address
	 *
	 * @throws NumberFormatException
	 *             if addr representation is invalid
	 */
	public static EUI48 valueOf(String addrStr)
			throws NumberFormatException {
		return new EUI48(addrStr);
	}

	/**
	 * Constructs a new EthernetAddress given the long int value (64-bit)
	 * representation of the ethernet address (of which 48 LSB contain the
	 * definition)
	 *
	 * Note that calling this method returns the same result as would using the
	 * matching constructor.
	 *
	 * @param addr
	 *            Long int representation of the ethernet address
	 */
	public static EUI48 valueOf(long addr) {
		return new EUI48(addr);
	}

	/**
	 * Factory method that locates a network interface that has a suitable mac
	 * address (ethernet cards, and things that emulate one), and return that
	 * address. If there are multiple applicable interfaces, one of them is
	 * returned; which one is returned is not specified. Method is meant for
	 * accessing an address needed to construct generator for time+location
	 * based UUID generation method.
	 *
	 * @return Ethernet address of one of interfaces system has; not including
	 *         local or loopback addresses; if one exists, null if no such
	 *         interfaces are found.
	 */
	public static EUI48 fromInterface() {
		try {
			Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces();
			while (en.hasMoreElements()) {
				NetworkInterface nint = en.nextElement();
				if (!nint.isLoopback()) {
					byte[] data = nint.getHardwareAddress();
					if (data != null && data.length == 6) {
						return new EUI48(data);
					}
				}
			}
		} catch (java.net.SocketException e) {
		}
		return null;
	}



	/*
	 * /**********************************************************************
	 * /* Conversions to raw types
	 * /**********************************************************************
	 */

	/**
	 * Returns 6 byte byte array that contains the binary representation of this
	 * ethernet address; byte 0 is the most significant byte (and so forth)
	 *
	 * @return 6 byte byte array that contains the binary representation
	 */
	public byte[] asByteArray() {
		byte[] result = new byte[6];
		toByteArray(result);
		return result;
	}

	/**
	 * Synonym to 'asByteArray()'
	 *
	 * @return 6 byte byte array that contains the binary representation
	 */
	public byte[] toByteArray() {
		return asByteArray();
	}

	public void toByteArray(byte[] array) {
		if (array.length < 6) {
			throw new IllegalArgumentException(
					"Too small array, need to have space for 6 bytes");
		}
		toByteArray(array, 0);
	}

	public void toByteArray(byte[] array, int pos) {
		if (pos < 0 || (pos + 6) > array.length) {
			throw new IllegalArgumentException("Illegal offset (" + pos
					+ "), need room for 6 bytes");
		}
		int i = (int) (_address >> 32);
		array[pos++] = (byte) (i >> 8);
		array[pos++] = (byte) i;
		i = (int) _address;
		array[pos++] = (byte) (i >> 24);
		array[pos++] = (byte) (i >> 16);
		array[pos++] = (byte) (i >> 8);
		array[pos] = (byte) i;
	}

	public long toLong() {
		return _address;
	}

	/*
	 * /**********************************************************************
	 * /* Accessors
	 * /**********************************************************************
	 */

	/**
	 * Method that can be used to check if this address refers to a multicast
	 * address. Such addresses are never assigned to individual network cards.
	 */
	public boolean isMulticastAddress() {
		return (((int) (_address >> 40)) & 0x01) != 0;
	}

	/**
	 * Method that can be used to check if this address refers to a
	 * "locally administered address" (see
	 * [http://en.wikipedia.org/wiki/MAC_address] for details). Such addresses
	 * are not assigned to individual network cards.
	 */
	public boolean isLocallyAdministeredAddress() {
		return (((int) (_address >> 40)) & 0x02) != 0;
	}

	/*
	 * /**********************************************************************
	 * /* Standard methods
	 * /**********************************************************************
	 */

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (o == null)
			return false;
		if (o.getClass() != getClass())
			return false;
		return ((EUI48) o)._address == _address;
	}

	/**
	 * Method that compares this EthernetAddress to one passed in as argument.
	 * Comparison is done simply by comparing individual address bytes in the
	 * order.
	 *
	 * @return negative number if this EthernetAddress should be sorted before
	 *         the parameter address if they are equal, os positive non-zero
	 *         number if this address should be sorted after parameter
	 */
	@Override
	public int compareTo(EUI48 other) {
		long l = _address - other._address;
		if (l < 0L)
			return -1;
		return (l == 0L) ? 0 : 1;
	}

	/**
	 * Returns the canonical string representation of this ethernet address.
	 * Canonical means that all characters are lower-case and string length is
	 * always 17 characters (ie. leading zeroes are not omitted).
	 *
	 * @return Canonical string representation of this ethernet address.
	 */
	@Override
	public String toString() {
		String str = _asString;
		if (str != null) {
			return str;
		}

		/*
		 * Let's not cache the output here (unlike with UUID), assuming this
		 * won't be called as often:
		 */
		StringBuilder b = new StringBuilder(17);
		int i1 = (int) (_address >> 32);
		int i2 = (int) _address;

		_appendHex(b, i1 >> 8);
		b.append(':');
		_appendHex(b, i1);
		b.append(':');
		_appendHex(b, i2 >> 24);
		b.append(':');
		_appendHex(b, i2 >> 16);
		b.append(':');
		_appendHex(b, i2 >> 8);
		b.append(':');
		_appendHex(b, i2);
		_asString = str = b.toString();
		return str;
	}

	private void _appendHex(StringBuilder sb, int hex) {
		sb.append(HEX_CHARS[(hex >> 4) & 0xF]);
		sb.append(HEX_CHARS[(hex & 0x0f)]);
	}
}
