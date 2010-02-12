package com.artagon.xacml.v3.policy.type;


import com.artagon.xacml.util.Preconditions;

final class HexBinaryTypeImpl extends BaseAttributeType<HexBinaryType.HexBinaryValue> implements HexBinaryType
{
	
	public HexBinaryTypeImpl(String typeId){
		super(typeId);
	}
	
	@Override
	public boolean isConvertableFrom(Object any) {
		return byte[].class.isInstance(any) || String.class.isInstance(any);
	}
	
	@Override
	public HexBinaryValue create(Object any, Object ...params){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"hexBinary\" type", 
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		if(byte[].class.isInstance(any)){
			return new HexBinaryValue(this, new BinaryValue((byte[])any));
		}
		return new HexBinaryValue(this, (BinaryValue)any);
	}

	@Override
	public HexBinaryValue fromXacmlString(String v, Object ...params) {
		Preconditions.checkNotNull(v);
		return create(hexToBin(v));
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
