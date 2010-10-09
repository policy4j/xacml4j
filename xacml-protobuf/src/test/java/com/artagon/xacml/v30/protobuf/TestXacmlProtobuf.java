package com.artagon.xacml.v30.protobuf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;

import com.artagon.xacml.v3.types.IntegerType;

import com.artagon.xacml.v30.protbuf.Xacml30.Attribute;
import com.artagon.xacml.v30.protbuf.Xacml30.AttributeDataTypeId;
import com.artagon.xacml.v30.protbuf.Xacml30.AttributeValue;

public class TestXacmlProtobuf 
{
	@Test
	public void test() throws IOException
	{
		Collection<AttributeValue> values = new LinkedList<AttributeValue>();
		values.add(AttributeValue.newBuilder().setType(AttributeDataTypeId.INTEGER).setIntVal(IntegerType.INTEGER.create(10).getValue()).build());
		values.add(AttributeValue.newBuilder().setType(AttributeDataTypeId.INTEGER).setIntVal(IntegerType.INTEGER.create(11).getValue()).build());
		values.add(AttributeValue.newBuilder().setType(AttributeDataTypeId.INTEGER).setIntVal(IntegerType.INTEGER.create(12).getValue()).build());
		Attribute a0 = Attribute.newBuilder().setId("test").setIssuer("test").addAllValues(values).build();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		a0.writeTo(out);
		System.out.println(out.toByteArray().length);
	}
}
