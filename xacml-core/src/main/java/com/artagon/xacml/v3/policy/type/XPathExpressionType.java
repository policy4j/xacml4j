package com.artagon.xacml.v3.policy.type;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.policy.AttributeValueType;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.XPathEvaluationException;

public interface XPathExpressionType extends AttributeValueType
{
	
	XPathExpressionValue create(Object v, Object ...params);
	XPathExpressionValue create(String xpath, AttributeCategoryId category);
	XPathExpressionValue fromXacmlString(String v, Object ...params);
	
	public class XPathExpressionValue extends BaseAttributeValue<XPathExpression>
	{
		private AttributeCategoryId categoryId;
		
		public XPathExpressionValue(XPathExpressionType type, 
				XPathExpression xpath, AttributeCategoryId categoryId){
			super(type, xpath);
		}
		
		public AttributeCategoryId getAttributeCategory(){
			return categoryId;
		}
		
		public Number evaluateToNumber(EvaluationContext context) throws EvaluationException
		{
			try
			{
				Node node = context.getContent(categoryId);
				return (Number)getValue().evaluate(node, XPathConstants.NUMBER);
			}catch(XPathExpressionException e){
				throw new XPathEvaluationException(e);
			}
		}
	}
}
