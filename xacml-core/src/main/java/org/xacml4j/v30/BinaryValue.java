package org.xacml4j.v30;

import java.io.Serializable;
import java.util.Arrays;

import org.xacml4j.util.Base64;
import org.xacml4j.util.Base64DecoderException;

import com.google.common.base.Preconditions;

/**
 * A base class for XACML binary values
 *
 * @author Giedrius Trumpickas
 */
public class BinaryValue implements Serializable
{
	private static final long serialVersionUID = 798617605980758773L;

	private byte[] value;

	private BinaryValue(byte[] b){
		this(b, 0, b.length);
	}

	private BinaryValue(byte[] b, int srcPos, int length){
		Preconditions.checkArgument((b== null && length == 0) ||
				(b != null) && b.length >= length);
		this.value = new byte[(b==null)?0:length];
		System.arraycopy(b, srcPos, value, 0, length);
	}

	public static BinaryValue valueOf(byte[] data){
		Preconditions.checkNotNull(data);
		return new BinaryValue(data);
	}

	public static BinaryValue valueOf(byte[] data, int srcPos, int length){
		return new BinaryValue(data, srcPos, length);
	}

	public static BinaryValue valueOfHexEnc(String v){
		Preconditions.checkNotNull(v);
		byte[] bin = hexToBin(v);
		return new BinaryValue(bin);
	}

	public static BinaryValue valueOfBase64Enc(String v){
		Preconditions.checkNotNull(v);
		try{
			byte[] bin = Base64.decode(v);
			return new BinaryValue(bin);
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
	    	if(!(o instanceof BinaryValue)){
	    		return false;
	    	}
	    	BinaryValue v = (BinaryValue)o;
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
