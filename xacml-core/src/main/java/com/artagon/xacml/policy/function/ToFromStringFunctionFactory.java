package com.artagon.xacml.policy.function;

import java.util.List;

import com.artagon.xacml.DataTypes;
import com.artagon.xacml.FunctionId;
import com.artagon.xacml.Functions;
import com.artagon.xacml.policy.Attribute;
import com.artagon.xacml.policy.AttributeDataType;
import com.artagon.xacml.policy.BaseFunctionInvocation;
import com.artagon.xacml.policy.DataTypeFactory;
import com.artagon.xacml.policy.EvaluationContext;
import com.artagon.xacml.policy.ExplicitFunctionSpecBuilder;
import com.artagon.xacml.policy.Expression;
import com.artagon.xacml.policy.FunctionSpec;
import com.artagon.xacml.policy.PolicyEvaluationException;
import com.artagon.xacml.policy.Value;
import com.artagon.xacml.policy.type.StringType;

public class ToFromStringFunctionFactory extends BaseFunctionFacatory
{

	public ToFromStringFunctionFactory(DataTypeFactory typeRegistry) 
	{
		super(typeRegistry);
		add(buildToString(Functions.STRING_FROM_ANYURI, getDataType(DataTypes.ANYURI)));
		add(buildToString(Functions.STRING_FROM_BOOLEAN, getDataType(DataTypes.BOOLEAN)));
		add(buildToString(Functions.STRING_FROM_INTEGER, getDataType(DataTypes.INTEGER)));
		add(buildToString(Functions.STRING_FROM_DOUBLE, getDataType(DataTypes.DOUBLE)));
		add(buildToString(Functions.STRING_FROM_DATE, getDataType(DataTypes.DATE)));
		add(buildToString(Functions.STRING_FROM_DATETIME, getDataType(DataTypes.DATETIME)));
		add(buildToString(Functions.STRING_FROM_DAYTIMEDURATION, getDataType(DataTypes.DAYTIMEDURATION)));
		add(buildToString(Functions.STRING_FROM_DNSNAME, getDataType(DataTypes.DNSNAME)));
		add(buildToString(Functions.STRING_FROM_IPADDRESS, getDataType(DataTypes.IPADDRESS)));
		
		add(buildFromString(Functions.ANYURI_FROM_STRING, getDataType(DataTypes.ANYURI)));
		add(buildFromString(Functions.BOOLEAN_FROM_STRING, getDataType(DataTypes.BOOLEAN)));
		add(buildFromString(Functions.INTEGER_FROM_STRING, getDataType(DataTypes.INTEGER)));
		add(buildFromString(Functions.DOUBLE_FROM_STRING, getDataType(DataTypes.DOUBLE)));
		add(buildFromString(Functions.DATE_FROM_STRING, getDataType(DataTypes.DATE)));
		add(buildFromString(Functions.DATETIME_FROM_STRING, getDataType(DataTypes.DATETIME)));
		add(buildFromString(Functions.DNSNAME_FROM_STRING, getDataType(DataTypes.DNSNAME)));
		add(buildFromString(Functions.IPADDRESS_FROM_STRING, getDataType(DataTypes.IPADDRESS)));
	}
	
	private FunctionSpec buildToString(FunctionId functionId, final AttributeDataType type)
	{
		ExplicitFunctionSpecBuilder builder = new ExplicitFunctionSpecBuilder(functionId);
		final StringType returnType = getDataType(DataTypes.STRING);
		builder.withParam(type).withReturnType(returnType);
		return builder.build(new BaseFunctionInvocation() {
			@Override
			protected Value doInvoke(EvaluationContext context, List<Expression> exp)
					throws PolicyEvaluationException {
				Attribute v = (Attribute)exp.get(0);
				return returnType.create(v.toXacmlString());
			}
		});
	}
	
	private FunctionSpec buildFromString(FunctionId functionId, final AttributeDataType returnType)
	{
		ExplicitFunctionSpecBuilder builder = new ExplicitFunctionSpecBuilder(functionId);
		final StringType paramType = getDataType(DataTypes.STRING);
		builder.withParam(paramType).withReturnType(returnType);
		return builder.build(new BaseFunctionInvocation() {
			@Override
			protected Value doInvoke(EvaluationContext context, List<Expression> exp)
					throws PolicyEvaluationException {
				Attribute v = (Attribute)exp.get(0);
				return returnType.create(v.toXacmlString());
			}
		});
	}
	
}
