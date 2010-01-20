package com.artagon.xacml;


/**
 * This enumeration defines the identifiers for the DataTypes that are used 
 * in the XACML specification:
 * <br>
 * <b>
 * http://docs.oasis-open.org/xacml/2.0/access_control-xacml-2.0-core-spec-os.pdf 
 * </b>
 * <br>
 * See Appendix A.2 ("Data-types") of the XACML 2.0 specification for 
 * the source of these DataType definitions and subsequent sections for
 * detail on the semantics of these data types.
 */
public enum DataTypes implements DataTypeId
{
	/** XACML DataType:  <b>http://www.w3.org/2001/XMLSchema#anyURI</b> */ 
	ANYURI("http://www.w3.org/2001/XMLSchema#anyURI"),
	
	/** XACML DataType:  <b>http://www.w3.org/2001/XMLSchema#base64Binary</b> */
	BASE64BINARY("http://www.w3.org/2001/XMLSchema#base64Binary"),
	
	/** XACML DataType:  <b>http://www.w3.org/2001/XMLSchema#boolean</b> */
	BOOLEAN("http://www.w3.org/2001/XMLSchema#boolean"),
	
	/** XACML DataType:  <b>http://www.w3.org/2001/XMLSchema#date</b> */
	DATE("http://www.w3.org/2001/XMLSchema#date"),
	
	/** XACML DataType:  <b>http://www.w3.org/2001/XMLSchema#dateTime</b> */
	DATETIME("http://www.w3.org/2001/XMLSchema#dateTime"),
	
	/** XACML DataType:  <b>urn:oasis:names:tc:xacml:2.0:data-type:dayTimeDuration</b> */
	DAYTIMEDURATION("urn:oasis:names:tc:xacml:2.0:data-type:dayTimeDuration"),
	 
	DNSNAME("urn:oasis:names:tc:xacml:2.0:data-type:dnsName"),
	
	/** XACML DataType:  <b>http://www.w3.org/2001/XMLSchema#double</b> */
	DOUBLE("http://www.w3.org/2001/XMLSchema#double"),
	
	/** XACML DataType:  <b>http://www.w3.org/2001/XMLSchema#hexBinary</b> */
	HEXBINARY("http://www.w3.org/2001/XMLSchema#hexBinary"),
	
	/** XACML DataType:  <b>http://www.w3.org/2001/XMLSchema#integer</b> */
	INTEGER("http://www.w3.org/2001/XMLSchema#integer"),
	 
	IPADDRESS("urn:oasis:names:tc:xacml:2.0:data-type:ipAddress"),
	
	/** XACML DataType:  <b>http://www.w3.org/2001/XMLSchema#string</b> */ 
	RFC822NAME("urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name"),
	
	/** XACML DataType:  <b>http://www.w3.org/2001/XMLSchema#string</b> */
	STRING("http://www.w3.org/2001/XMLSchema#string"),
	
	/** XACML DataType:  <b>http://www.w3.org/2001/XMLSchema#time</b> */
	TIME("http://www.w3.org/2001/XMLSchema#time"),
	
	X500NAME("urn:oasis:names:tc:xacml:1.0:data-type:x500Name"),
	
	/** XACML DataType:  <b>urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression</b> */
	XPATHEXPRESSION("urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression"),
	
	/** XACML DataType:  <b>urn:oasis:names:tc:xacml:2.0:data-type:yearMonthDuration</b> */
	YEARMONTHDURATION("urn:oasis:names:tc:xacml:2.0:data-type:yearMonthDuration");
	
	private final String dataTypeId;
	
	DataTypes(String azDataTypeId){
		this.dataTypeId = azDataTypeId;
	}
	
	@Override public String toString() {
		return dataTypeId;
	}
	
	public static DataTypeId parse(final String v){
		for(DataTypes typeId : values()){
			if(typeId.dataTypeId.equals(v)){
				return typeId;
			}
		}
		return null;
	}
}
