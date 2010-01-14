package com.artagon.xacml.policy.type;

public class DefaultDataTypeFactory extends BaseDataTypeFactory
{

	public DefaultDataTypeFactory()
	{
		super();
		add(new AnyURITypeImpl());
		add(new BooleanTypeImpl());
		add(new DNSNameTypeImpl());
		add(new DoubleTypeImpl());
		add(new HexBinaryTypeImpl());
		add(new Base64BinaryTypeImpl());
		add(new IntegerTypeImpl());
		add(new IPAddressTypeImpl());
		add(new RFC822NameTypeImpl());
		add(new StringTypeImpl());
		add(new X500NameTypeImpl());
		add(new DateTimeTypeImpl());
		add(new DateTypeImpl());
	}
	
}
