package com.artagon.xacml.v3.policy.impl.function;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.XPathEvaluationException;
import com.artagon.xacml.v3.policy.XPathProvider;
import com.artagon.xacml.v3.policy.annotations.XacmlFunc;
import com.artagon.xacml.v3.policy.annotations.XacmlFuncReturnType;
import com.artagon.xacml.v3.policy.annotations.XacmlParam;
import com.artagon.xacml.v3.policy.annotations.XacmlParamEvaluationContext;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.IntegerType.IntegerValue;
import com.artagon.xacml.v3.policy.type.XPathExpressionType.XPathExpressionValue;

public class XPathFunctions 
{
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:xpath-node-count")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue xpathCount(
			@XacmlParamEvaluationContext EvaluationContext context,
			@XacmlParam(type=DataTypes.XPATHEXPRESSION) XPathExpressionValue xpath) 
	{
		XPathProvider xpathProvider = context.getXPathProvider();
		Node content = context.getContent(xpath.getAttributeCategory());
		if(content == null){
			return DataTypes.INTEGER.create(0);
		}
		try{
			String n = xpathProvider.evaluateToString(xpath.getValue(), content);
			if(n != null){
				return DataTypes.INTEGER.create(n);
			}
			return DataTypes.INTEGER.create(0);
		}catch(XPathEvaluationException e){
			return DataTypes.INTEGER.create(0);
		}
	}
}
