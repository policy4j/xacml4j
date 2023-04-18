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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

import org.xacml4j.util.Base64;
import org.xacml4j.util.Base64DecoderException;

import com.google.common.base.Preconditions;

/**
 * A base class for XACML binary values
 *
 * @author Giedrius Trumpickas
 */
public final class Binary implements Externalizable
{
	private static final long serialVersionUID = 798617605980758773L;

	private byte[] value;

	private Binary(byte[] b){
		this(b, 0, b.length);
	}

	private Binary(byte[] b, int srcPos, int length){
		Preconditions.checkArgument((b== null && length == 0) ||
				(b != null) && b.length >= length);
		this.value = new byte[(b==null)?0:length];
		System.arraycopy(b, srcPos, value, 0, length);
	}

	public static Binary of(byte[] data){
		Preconditions.checkNotNull(data);
		return new Binary(data);
	}

	public static Binary of(byte[] data, int srcPos, int length){
		return new Binary(data, srcPos, length);
	}

	public static Binary valueOfHexEnc(String v){
		Preconditions.checkNotNull(v);
		byte[] bin = hexToBin(v);
		return new Binary(bin);
	}

	public static Binary valueOfBase64Enc(String v){
		Preconditions.checkNotNull(v);
		try{
			byte[] bin = Base64.decode(v);
			return new Binary(bin);
		}catch(Base64DecoderException e){
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public int hashCode(){
		return Arrays.hashCode(value);
	}

	@Override
	public boolean equals(Object o){
	    	if(o == null){
	    		return false;
	    	}
	    	if(o == this){
	    		return true;
	    	}
	    	if(!(o instanceof Binary)){
	    		return false;
	    	}
	    	Binary v = (Binary)o;
	    	return Arrays.equals(value, v.value);
	}

	@Override
	public String toString(){
		return Arrays.toString(value);
	}

	/**
	 * Encodes this binary value
	 * to hexadecimal representation
	 *
	 * @return a hexadecimal representation
	 * of this binary value
	 */
	public String toHexEncoded(){
		return binToHex(value);
	}

	/**
	 * Encodes this binary value
	 * to base64 representation
	 *
	 * @return a base64 representation
	 * of this binary value
	 */
	public String toBase64Encoded(){
		return Base64.encode(value);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.write(value);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.value = new byte[in.available()];
		in.readFully(this.value);
	}

	/**
     * Return the hex character for a particular nibble (half a byte).
     *
     * @param nibble a value 0-15
     * @return hex character for that nibble (using A-F for 10-15)
     */
    private static char binToHexNibble(int nibble) {
        return (nibble < 10)?(char) (nibble + '0'):(char) ((nibble - 10) + 'A');
    }


    /**
     * Return a straight hexadecimal conversion of a byte array.
     * This is a String containing only hex digits.
     *
     * @param bytes the byte array
     * @return the hex version
     */
    private static String binToHex(byte [] bytes) {
        int byteLength = bytes.length;
        char [] chars = new char [byteLength * 2];
        int charIndex = 0;

	    for (byte b : bytes) {
		    chars[charIndex++] = binToHexNibble((b >> 4) & 0xf);
		    chars[charIndex++] = binToHexNibble(b & 0xf);
	    }

        return new String(chars);
    }


    /**
     * Return the int value of a hex character. Return -1 if the
     * character is not a valid hex character.
     */
    private static int hexToBinNibble(char c) {
        int result = -1;

        if ((c >= '0') && (c <= '9'))
            result = (c - '0');
        else {
            if ((c >= 'a') && (c <= 'f'))
                result = (c - 'a') + 10;
            else {
                if ((c >= 'A') && (c <= 'F'))
                    result = (c - 'A') + 10;
                // else pick up the -1 value set above
            }
        }
        return result;
    }

    /**
     * Parse a hex string, returning a new byte array containing the
     * value. Return null in case of a parsing error.
     *
     * @param hex the hex string
     * @return a new byte array containing the value (or null)
     */
    private static byte [] hexToBin(String hex) {
        int len = hex.length();
        // Must have an even number of hex digits
        if (len % 2 != 0)
            return null;
        int byteCount = len / 2;
        byte [] bytes = new byte [byteCount];

        int charIndex = 0;
        for (int byteIndex = 0; byteIndex < byteCount; byteIndex++) {
            int hiNibble = hexToBinNibble(hex.charAt(charIndex++));
            int loNibble = hexToBinNibble(hex.charAt(charIndex++));
            if ((hiNibble < 0) || (loNibble < 0))
                return null;
            bytes[byteIndex] = (byte) (hiNibble * 16 + loNibble);
        }
        return bytes;
    }
}
