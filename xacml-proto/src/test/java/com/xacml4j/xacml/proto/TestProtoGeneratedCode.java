package com.xacml4j.xacml.proto;

/*
 * #%L
 * Xacml4J Google Protbuf 3 based request/response and policy format support
 * %%
 * Copyright (C) 2009 - 2023 Xacml4J.org
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

import org.junit.Test;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;


public class TestProtoGeneratedCode
{
	@Test
	public void test() throws Exception{
		PAttribute attribute = PAttribute.newBuilder()
		                                 .setIssuer("test")
		                                 .setId("id")
		                                 .build();
		PEntity entity = PEntity.newBuilder()
		                        .addAttributes(attribute)
		                        .build();
		PValue value = PValue.newBuilder()
		                     .setType(XacmlTypes.ANYURI)
		                     .setValue(Any.parseFrom(ByteString.copyFromUtf8("test")))
		                     .build();
		PExpression expression = PExpression.newBuilder()
		                                    .setType(ExpressionType.VALUE_EXP)
		                                    .setExpression(Any.pack(value))
		                                    .build();
	}
}
