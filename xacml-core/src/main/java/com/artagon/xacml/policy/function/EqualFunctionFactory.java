package com.artagon.xacml.policy.function;

import java.util.List;

import com.artagon.xacml.DataTypes;
import com.artagon.xacml.FunctionId;
import com.artagon.xacml.Functions;
import com.artagon.xacml.policy.AttributeDataType;
import com.artagon.xacml.policy.DataTypeFactory;
import com.artagon.xacml.policy.EvaluationContext;
import com.artagon.xacml.policy.ExplicitFunctionSpecBuilder;
import com.artagon.xacml.policy.Expression;
import com.artagon.xacml.policy.FunctionSpec;
import com.artagon.xacml.policy.PolicyEvaluationException;
import com.artagon.xacml.policy.RegularFunction;
import com.artagon.xacml.policy.Value;
import com.artagon.xacml.policy.ValueType;
import com.artagon.xacml.policy.type.BooleanType;

public class EqualFunctionFactory extends BaseFunctionFacatory
{
	public EqualFunctionFactory(DataTypeFactory typeRegistry)
	{
		super(typeRegistry);
		add(build(Functions.ANYURI_EQUAL, typeRegistry.getDataType(DataTypes.ANYURI)));
		add(build(Functions.BOOLEAN_EQUAL, typeRegistry.getDataType(DataTypes.BOOLEAN)));
		add(build(Functions.INTEGER_EQUAL, typeRegistry.getDataType(DataTypes.INTEGER)));
		add(build(Functions.DOUBLE_EQUAL, typeRegistry.getDataType(DataTypes.DOUBLE)));
		add(build(Functions.STRING_EQUAL, typeRegistry.getDataType(DataTypes.STRING)));
		add(build(Functions.RFC833NAME_EQUAL, typeRegistry.getDataType(DataTypes.RFC822NAME)));
		add(build(Functions.HEXBINARY_EQUAL, typeRegistry.getDataType(DataTypes.HEXBINARY)));
		add(build(Functions.BASE64BINARY_EQUAL, typeRegistry.getDataType(DataTypes.BASE64BINARY)));
		//add(build(Functions.DATE_EQUAL, typeRegistry.getDataType(DataTypes.DATE)));
		//add(build(Functions.DATETIME_EQUAL, typeRegistry.getDataType(DataTypes.DATETIME)));
		add(build(Functions.X500NAME_EQUAL, typeRegistry.getDataType(DataTypes.X500NAME)));
	}
	
	private FunctionSpec build(FunctionId functionId, AttributeDataType type)
	{
		ExplicitFunctionSpecBuilder builder = new ExplicitFunctionSpecBuilder(functionId);
		final BooleanType returnType = getDataType(DataTypes.BOOLEAN);
		builder.withParam(type).withParam(type);
		return builder.build(new RegularFunction() {
			
			@Override
			public Value invoke(EvaluationContext context, List<Expression> exp)
					throws PolicyEvaluationException {
				return returnType.create(exp.get(0).equals(exp.get(1)));
			}
			
			@Override
			public ValueType getReturnType() {
				return returnType;
			}
		});
	}
}
