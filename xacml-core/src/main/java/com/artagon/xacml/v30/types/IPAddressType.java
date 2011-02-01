package com.artagon.xacml.v30.types;

import java.net.InetAddress;
import java.util.Collection;

import com.artagon.xacml.util.IPAddressUtils;
import com.artagon.xacml.v30.AttributeValue;
import com.artagon.xacml.v30.AttributeValueType;
import com.artagon.xacml.v30.BagOfAttributeValues;
import com.artagon.xacml.v30.BagOfAttributeValuesType;
import com.google.common.base.Preconditions;

/** 
 * XACML DataType:  <b>urn:oasis:names:tc:xacml:2.0:data-type:ipAddress</b>. 
 * <br>The "urn:oasis:names:tc:xacml:2.0:data-type:ipAddress" primitive 
 * type represents an IPv4 or IPv6 network address, with optional mask 
 * and optional port or port range. The syntax SHALL be:
 * <pre>
 *     ipAddress = address [ "/" mask ] [ ":" [ portrange ] ]
 * </pre>
 * For an IPv4 address, the address and mask are formatted in accordance 
 * with the syntax for a "host" in IETF RFC 2396 "Uniform Resource Identifiers 
 * (URI): Generic Syntax", section 3.2. 
 * <p>
 * For an IPv6 address, the address and mask are formatted in accordance with 
 * the syntax for an "ipv6reference" in IETF RFC 2732 "Format for Literal IPv6 
 * Addresses in URL's". (Note that an IPv6 address or mask, in this syntax, 
 * is enclosed in literal "[" "]" brackets.) 
 */
public enum IPAddressType implements AttributeValueType
{
	IPADDRESS("urn:oasis:names:tc:xacml:2.0:data-type:ipAddress");
	
	private String typeId;
	private BagOfAttributeValuesType bagType;
	
	private IPAddressType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeValuesType(this);
	}
	
	public IPAddressValue create(InetAddress address, PortRange portRange) {
		return new IPAddressValue(this, address, portRange);
	}

	public IPAddressValue create(InetAddress address, InetAddress mask) {
		Preconditions.checkNotNull(address, "IP address can't be null");
		Preconditions.checkNotNull(mask, "IP address mask can't be null");
		return new IPAddressValue(this, address, mask, PortRange.getAnyPort());
	}

	public IPAddressValue create(InetAddress address, InetAddress mask,
			PortRange portRange) {
		Preconditions.checkNotNull(address, "IP address can't be null");
		Preconditions.checkNotNull(mask, "IP address mask can't be null");
		Preconditions.checkNotNull(portRange, "Port range can't be null");
		return new IPAddressValue(this, address, mask, portRange);
	}
	
	@Override
	public boolean isConvertableFrom(Object any) {
		return String.class.isInstance(any) 
		|| InetAddress.class.isInstance(any);
	}

	@Override
	public IPAddressValue create(Object any, Object ...params) {
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" can't ne " +
				"converted to XACML \"ipAddress\" type", 
				any, any.getClass()));
		if(any instanceof InetAddress){
			return new IPAddressValue(this, (InetAddress)any);
		}
		if(any instanceof String){
			return fromXacmlString((String)any);
		}
		return new IPAddressValue(this, (InetAddress)any);
	}
	
	private IPAddressValue getV6Instance(String value)
    {
		 InetAddress address = null;
		 InetAddress mask = null;
		 PortRange range = null;
		 int len = value.length();
		 int endIndex = value.indexOf(']');
		 String addrr = value.substring(1, endIndex);
		 Preconditions.checkArgument(IPAddressUtils.isIPv6LiteralAddress(addrr), 
         		  "Expected IPV6 address, but found=\"%s\"", addrr);
		 address = IPAddressUtils.parseAddress(addrr);
      
      // see if there's anything left in the string
      if (endIndex != (len - 1)) {
          // if there's a mask, it's also an IPv6 address
          if (value.charAt(endIndex + 1) == '/') {
              int startIndex = endIndex + 3;
              endIndex = value.indexOf(']', startIndex);
              addrr = value.substring(startIndex, endIndex);
              Preconditions.checkArgument(IPAddressUtils.isIPv6LiteralAddress(addrr), 
            		  "Expected IPV6 mask, but found=\"%s\"", addrr);
              mask = IPAddressUtils.parseAddress(addrr);
          }
          if ((endIndex != (len - 1)) && (value.charAt(endIndex + 1) == ':'))
              range = PortRange.valueOf(value.substring(endIndex + 2, len));
      }
      return new IPAddressValue(this, address, mask, (range != null)?range:PortRange.getAnyPort());
    }
	
    private IPAddressValue getV4Instance(String value)
    {
        InetAddress address = null;
        InetAddress mask = null;
        PortRange range = null;
        int maskPos = value.indexOf("/");
        int rangePos = value.indexOf(":");
        if (maskPos == rangePos) {
            // the sting is just an address
        	 Preconditions.checkArgument(IPAddressUtils.isIPv4LiteralAddress(value), 
            		  "Expected IPV4 address, but found=\"%s\"", value);
            address = IPAddressUtils.parseAddress(value);
        } else if (maskPos != -1) {
            // there is also a mask (and maybe a range)
        	String addrr = value.substring(0, maskPos);
        	 Preconditions.checkArgument(IPAddressUtils.isIPv4LiteralAddress(addrr), 
           		  "Expected IPV4 address, but found=\"%s\"", addrr);
            address = IPAddressUtils.parseAddress(value.substring(0, maskPos));
            if (rangePos != -1) {
                // there's a range too, so get it and the mask
                mask = IPAddressUtils.parseAddress(value.substring(maskPos + 1,
                                                          rangePos));
                range =
                    PortRange.valueOf(value.substring(rangePos + 1,
                                                          value.length()));
            } else {
                // there's no range, so just get the mask
                mask = IPAddressUtils.parseAddress(value.substring(maskPos + 1,
                                                             value.length()));
            }
        } else {
            // there is a range, but no mask
            address = IPAddressUtils.parseAddress(value.substring(0, rangePos));
            range = PortRange.valueOf(value.substring(rangePos + 1,
                                                          value.length()));
        }
        return new IPAddressValue(this, address, mask, range != null?range:PortRange.getAnyPort());
    }

	@Override
	public IPAddressValue fromXacmlString(String v, Object ...params) 
	{
		v = v.trim();
		return (v.indexOf('[') == 0)?getV6Instance(v):getV4Instance(v);
	}
	
	@Override
	public String getDataTypeId() {
		return typeId;
	}

	@Override
	public BagOfAttributeValuesType bagType() {
		return bagType;
	}

	@Override
	public BagOfAttributeValues bagOf(AttributeValue... values) {
		return bagType.create(values);
	}

	@Override
	public BagOfAttributeValues bagOf(Collection<AttributeValue> values) {
		return bagType.create(values);
	}
	
	@Override
	public BagOfAttributeValues bagOf(Object... values) {
		return bagType.bagOf(values);
	}
	
	@Override
	public BagOfAttributeValues emptyBag() {
		return bagType.createEmpty();
	}
	
	@Override
	public String toString(){
		return typeId;
	}
}