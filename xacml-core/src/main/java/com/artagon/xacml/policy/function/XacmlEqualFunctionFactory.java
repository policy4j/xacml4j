package com.artagon.xacml.policy.function;

import com.artagon.xacml.policy.AttributeDataType;
import com.artagon.xacml.policy.EvaluationContext;
import com.artagon.xacml.policy.Expression;
import com.artagon.xacml.policy.FunctionSpec;
import com.artagon.xacml.policy.PolicyEvaluationException;
import com.artagon.xacml.policy.Value;
import com.artagon.xacml.policy.ValueType;
import com.artagon.xacml.policy.type.XacmlDataType;
import com.artagon.xacml.policy.type.BooleanType.BooleanValue;

public class XacmlEqualFunctionFactory extends BaseFunctionFacatory
{
	
	public XacmlEqualFunctionFactory()
	{
		add(build(XacmlFunction.ANYURI_EQUAL, XacmlDataType.ANYURI.getType()));
		add(build(XacmlFunction.BOOLEAN_EQUAL, XacmlDataType.BOOLEAN.getType()));
		add(build(XacmlFunction.INTEGER_EQUAL, XacmlDataType.INTEGER.getType()));
		add(build(XacmlFunction.DOUBLE_EQUAL, XacmlDataType.DOUBLE.getType()));
		add(build(XacmlFunction.STRING_EQUAL, XacmlDataType.STRING.getType()));
		add(build(XacmlFunction.RFC833NAME_EQUAL, XacmlDataType.RFC822NAME.getType()));
		add(build(XacmlFunction.HEXBINARY_EQUAL, XacmlDataType.HEXBINARY.getType()));
		add(build(XacmlFunction.BASE64BINARY_EQUAL, XacmlDataType.BASE64BINARY.getType()));
		add(build(XacmlFunction.DATE_EQUAL, XacmlDataType.DATE.getType()));
		add(build(XacmlFunction.DATETIME_EQUAL, XacmlDataType.DATETIME.getType()));
		add(build(XacmlFunction.TIME_EQUAL, XacmlDataType.TIME.getType()));
		add(build(XacmlFunction.DAYTIMEDURATION_EQUAL, XacmlDataType.DAYTIMEDURATION.getType()));
		add(build(XacmlFunction.YEARMONTHDURATION_EQUAL, XacmlDataType.YEARMONTHDURATION.getType()));
		add(build(XacmlFunction.X500NAME_EQUAL, XacmlDataType.X500NAME.getType()));
	}
	
	private FunctionSpec build(XacmlFunction functionId, AttributeDataType type)
	{
		ExplicitFunctionSpecBuilder builder = new ExplicitFunctionSpecBuilder(functionId.getXacmlId());
		final BooleanValue FALSE = XacmlDataType.BOOLEAN.create(Boolean.FALSE);
		final BooleanValue TRUE = XacmlDataType.BOOLEAN.create(Boolean.TRUE);
		builder.withParam(type).withParam(type);
		return builder.build(new StaticallyTypedFunction()
		{
			@Override
			public Value invoke(EvaluationContext context, Expression ...args)
					throws PolicyEvaluationException {
				return args[0].equals(args[1])?TRUE:FALSE;
			}
			
			@Override
			public ValueType getReturnType() {
				return XacmlDataType.BOOLEAN.getType();
			}
		});
	}
}
