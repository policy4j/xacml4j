package com.artagon.xacml.v30.protobuf;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;

import com.artagon.xacml.v30.types.IntegerType;

import com.artagon.xacml.v30.protbuf.Xacml30.PAttribute;
import com.artagon.xacml.v30.protbuf.Xacml30.PAttributeDataTypeId;
import com.artagon.xacml.v30.protbuf.Xacml30.PAttributeValue;

public class TestXacmlProtobuf 
{
	@Test
	public void test() throws IOException
	{
		Collection<PAttributeValue> values = new LinkedList<PAttributeValue>();
		values.add(PAttributeValue.newBuilder().setType(PAttributeDataTypeId.INTEGER).setIntVal(IntegerType.INTEGER.create(10).getValue()).build());
		values.add(PAttributeValue.newBuilder().setType(PAttributeDataTypeId.INTEGER).setIntVal(IntegerType.INTEGER.create(11).getValue()).build());
		values.add(PAttributeValue.newBuilder().setType(PAttributeDataTypeId.INTEGER).setIntVal(IntegerType.INTEGER.create(12).getValue()).build());
		PAttribute a0 = PAttribute.newBuilder().setId("test").setIssuer("test").addAllValues(values).build();
		
		System.out.println(a0.getSerializedSize());
	}
}
