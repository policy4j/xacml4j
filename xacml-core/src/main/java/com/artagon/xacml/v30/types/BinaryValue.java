package com.artagon.xacml.v30.types;

import java.util.Arrays;

import com.artagon.xacml.util.Base64;

/**
 * A base class for XACML binary values
 * 
 * @author Giedrius Trumpickas
 */
public class BinaryValue
{
	private byte[] value;
	
	public BinaryValue(byte[] value){
		this.value = value;
	}
	
	@Override
	public int hashCode(){
		return Arrays.hashCode(value);
	}
	
	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof BinaryValue)){
			return false;
		}
		BinaryValue v = (BinaryValue)o;
		return Arrays.equals(value, v.value);
	}
	
	public String toHex(){
		return binToHex(value);
	}
	
	public String toBase64(){
		return Base64.encode(value);
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
        
        for (int byteIndex = 0; byteIndex < byteLength; byteIndex++) {
            byte b = bytes[byteIndex];
            chars[charIndex++] = binToHexNibble((b >> 4) & 0xf);
            chars[charIndex++] = binToHexNibble(b & 0xf);
        }

        return new String(chars);
    }
}
